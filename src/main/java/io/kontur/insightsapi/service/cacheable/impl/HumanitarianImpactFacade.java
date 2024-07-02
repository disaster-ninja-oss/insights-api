package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.HumanitarianImpactDto;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.HumanitarianImpactService;
import io.kontur.insightsapi.service.cacheable.RedisLock;
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
@ConditionalOnProperty(prefix = "cache", name = "humanitarian-impact")
@RequiredArgsConstructor
public class HumanitarianImpactFacade implements HumanitarianImpactService, CacheEvictable {

    private final PopulationTransformer populationTransformer;

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "humanitarian-impact", keyGenerator = "stringKeyGenerator")
    public List<HumanitarianImpactDto> calculateHumanitarianImpact(String geojson) {
        return populationTransformer.calculateHumanitarianImpact(geojson);
    }

    @Override
    @CacheEvict(value = "humanitarian-impact", allEntries = true)
    public void evict() {
    }
}
