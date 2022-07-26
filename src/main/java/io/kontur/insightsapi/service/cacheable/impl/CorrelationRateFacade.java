package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.CorrelationRateService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "correlation-rate")
@RequiredArgsConstructor
public class CorrelationRateFacade implements CorrelationRateService, CacheEvictable {

    private final StatisticRepository repository;

    @SneakyThrows
    @Override
    @Cacheable(value = "correlation-rate-all", key = "'all'")
    public List<PolygonCorrelationRate> getAllCorrelationRateStatistics() {
        return repository.getAllCorrelationRateStatistics();
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "correlation-rate-num-den", key = "'all'")
    public List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation() {
        return repository.getNumeratorsDenominatorsForCorrelation();
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "correlation-rate-polygon", keyGenerator = "stringListKeyGenerator")
    public List<Double> getPolygonCorrelationRateStatisticsBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        return repository.getPolygonCorrelationRateStatisticsBatch(polygon, dtoList);
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "correlation-rate-num-not-empty-layers", keyGenerator = "stringListKeyGenerator")
    public Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        return repository.getNumeratorsForNotEmptyLayersBatch(polygon, dtoList);
    }

    @Override
    @CacheEvict(value = {"correlation-rate-all", "correlation-rate-num-den", "correlation-rate-polygon", "correlation-rate-num-not-empty-layers"},
            allEntries = true)
    public void evict() {
    }
}
