package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.dto.StatisticDto;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.PopulationService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "population")
public class PopulationFacade implements PopulationService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    private final Cache<String, StatisticDto> cache;

    private final HashFunction hashFunction;

    public PopulationFacade(PopulationTransformer populationTransformer, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.populationTransformer = populationTransformer;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public StatisticDto calculatePopulation(String geojson) {
        return cache.get(keyGen(geojson),
                () -> populationTransformer.calculatePopulation(geojson));
    }

    private String keyGen(String geojson) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()).toString();
    }

    @Override
    public void evict() {
        cache.invalidateAll();
    }
}
