package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsUuidCorrelationDto;
import io.kontur.insightsapi.model.PolygonMetrics;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.CorrelationRateService;
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
@ConditionalOnProperty(prefix = "cache", name = "correlation-rate")
@RequiredArgsConstructor
public class CorrelationRateFacade implements CorrelationRateService, CacheEvictable {

    private final StatisticRepository repository;

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "correlation-rate-all", key = "'all'")
    public List<PolygonMetrics> getAllCorrelationRateStatistics() {
        return repository.getAllCorrelationRateStatistics();
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "correlation-rate-polygon", keyGenerator = "stringListKeyGenerator")
    public List<Double> getPolygonCorrelationRateStatisticsBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        return repository.getPolygonCorrelationRateStatisticsBatch(polygon, dtoList);
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "correlation-rate-polygon-all", keyGenerator = "stringKeyGenerator")
    public List<NumeratorsDenominatorsUuidCorrelationDto> getPolygonCorrelationRateStatistics(String polygon) {
        return repository.getPolygonCorrelationRateStatistics(polygon);
    }

    @Override
    @CacheEvict(value = {"correlation-rate-all", "correlation-rate-polygon", "correlation-rate-polygon-all"},
            allEntries = true)
    public void evict() {
    }
}
