package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.model.OsmQuality;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.OsmQualityService;
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
@ConditionalOnProperty(prefix = "cache", name = "osm-quality")
@RequiredArgsConstructor
public class OsmQualityFacade implements OsmQualityService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    @SneakyThrows
    @Override
    @Cacheable(value = "osm-quality", keyGenerator = "stringListKeyGenerator")
    public OsmQuality calculateOsmQuality(String geojson, List<String> osmRequestFields) {
        return populationTransformer.calculateOsmQuality(geojson, osmRequestFields);
    }

    @Override
    @CacheEvict(value = "osm-quality", allEntries = true)
    public void evict() {

    }
}
