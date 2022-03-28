package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.model.UrbanCore;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.UrbanCoreService;
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
@ConditionalOnProperty(prefix = "cache", name = "urban-core")
public class UrbanCoreFacade implements UrbanCoreService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    private final Cache<String, UrbanCore> cache;

    private final HashFunction hashFunction;

    public UrbanCoreFacade(PopulationTransformer populationTransformer, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.populationTransformer = populationTransformer;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public UrbanCore calculateUrbanCore(String geojson, List<String> requestFields) {
        return cache.get(keyGen(geojson, requestFields),
                () -> populationTransformer.calculateUrbanCore(geojson, requestFields));
    }

    private String keyGen(String geojson, List<String> requestFields) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()) + "_"
                + hashFunction.hashString(requestFields.toString(), Charset.defaultCharset());
    }

    @Override
    public void evict() {
        cache.invalidateAll();
    }
}
