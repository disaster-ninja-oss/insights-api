package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.StatisticDto;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.PopulationService;
import io.kontur.insightsapi.service.cacheable.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "population")
@RequiredArgsConstructor
public class PopulationFacade implements PopulationService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "population", keyGenerator = "stringKeyGenerator")
    public StatisticDto calculatePopulation(String geojson) {
        return populationTransformer.calculatePopulation(geojson);
    }

    @Override
    @CacheEvict(value = "population", allEntries = true)
    public void evict() {
    }
}
