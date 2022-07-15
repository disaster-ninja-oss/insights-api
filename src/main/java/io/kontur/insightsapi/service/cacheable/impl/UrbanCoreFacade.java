package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.model.UrbanCore;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.UrbanCoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "urban-core")
@RequiredArgsConstructor
public class UrbanCoreFacade implements UrbanCoreService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    @SneakyThrows
    @Override
    @Cacheable(value = "urban-core", keyGenerator = "stringListKeyGenerator")
    public UrbanCore calculateUrbanCore(String geojson, List<String> requestFields) {
        return populationTransformer.calculateUrbanCore(geojson, requestFields);
    }

    @Override
    @CacheEvict(value = "urban-core", allEntries = true)
    public void evict() {
    }
}
