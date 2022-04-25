package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.dto.HumanitarianImpactDto;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.HumanitarianImpactService;
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
@ConditionalOnProperty(prefix = "cache", name = "humanitarian-impact")
public class HumanitarianImpactFacade implements HumanitarianImpactService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    private final Cache<String, List<HumanitarianImpactDto>> cache;

    private final HashFunction hashFunction;

    public HumanitarianImpactFacade(PopulationTransformer populationTransformer, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.populationTransformer = populationTransformer;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public List<HumanitarianImpactDto> calculateHumanitarianImpact(String geojson) {
        return cache.get(keyGen(geojson),
                () -> populationTransformer.calculateHumanitarianImpact(geojson));
    }

    private String keyGen(String geojson) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()).toString();
    }

    @Override
    public void evict() {
        cache.invalidateAll();
    }
}
