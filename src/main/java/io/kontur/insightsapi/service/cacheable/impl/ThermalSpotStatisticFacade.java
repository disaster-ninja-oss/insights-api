package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.repository.ThermalSpotRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.ThermalSpotStatisticService;
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
@ConditionalOnProperty(prefix = "cache", name = "thermal-spot")
@RequiredArgsConstructor
public class ThermalSpotStatisticFacade implements ThermalSpotStatisticService, CacheEvictable {

    private final ThermalSpotRepository repository;

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "thermal-spot", keyGenerator = "stringListKeyGenerator")
    public ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList) {
        return repository.calculateThermalSpotStatistic(geojson, fieldList);
    }

    @Override
    @CacheEvict(value = "thermal-spot", allEntries = true)
    public void evict() {
    }
}
