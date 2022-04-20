package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.model.OsmQuality;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.OsmQualityService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "osm-quality")
public class OsmQualityFacade implements OsmQualityService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    private final Cache<String, OsmQuality> cache;

    private final HashFunction hashFunction;

    public OsmQualityFacade(PopulationTransformer populationTransformer, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.populationTransformer = populationTransformer;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public OsmQuality calculateOsmQuality(String geojson, List<String> osmRequestFields) {
        return cache.get(keyGen(geojson, osmRequestFields),
                () -> populationTransformer.calculateOsmQuality(geojson, osmRequestFields));
    }

    private String keyGen(String geojson, List<String> fieldList) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()) + "_"
                + hashFunction.hashString(fieldList.toString(), Charset.defaultCharset());
    }

    @Override
    public void evict() {
        cache.invalidateAll();
    }
}
