package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
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
@ConditionalOnProperty(prefix = "cache", name = "advanced-analytics")
@RequiredArgsConstructor
public class AdvancedAnalyticsFacade implements AdvancedAnalyticsService, CacheEvictable {

    private final AdvancedAnalyticsRepository repository;

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics-all", key = "'all'")
    public List<AdvancedAnalytics> getWorldData() {
        return repository.getWorldData();
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics-all", keyGenerator = "listKeyGenerator")
    public List<AdvancedAnalytics> getFilteredWorldData(List<AdvancedAnalyticsRequest> argRequests) {
        return repository.getFilteredWorldData(argRequests);
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics", keyGenerator = "stringStringKeyGenerator")
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        return repository.getAdvancedAnalytics(argQuery, argGeometry);
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics", keyGenerator = "threeParametersAsStringOrListKeyGenerator")
    public List<List<AdvancedAnalyticsValues>> getFilteredAdvancedAnalytics(String argQuery, String argGeometry, List<BivariativeAxisDto> axisDtos) {
        return repository.getFilteredAdvancedAnalytics(argQuery, argGeometry, axisDtos);
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics-v2", keyGenerator = "stringListKeyGenerator")
    public List<AdvancedAnalytics> getAdvancedAnalyticsV2(String argGeometry, List<BivariateIndicatorDto> indicators) {
        return repository.getAdvancedAnalyticsV2(argGeometry, indicators);
    }

    @SneakyThrows
    @Override
    @RedisLock
    @Cacheable(value = "advanced-analytics-v2", keyGenerator = "threeParametersAsStringOrListKeyGenerator")
    public List<AdvancedAnalytics> getFilteredAdvancedAnalyticsV2(String argGeometry, List<BivariateIndicatorDto> indicators, List<BivariativeAxisDto> axisDtos) {
        return repository.getFilteredAdvancedAnalyticsV2(argGeometry, indicators, axisDtos);
    }

    @Override
    @CacheEvict(value = {"advanced-analytics-all", "advanced-analytics", "advanced-analytics-v2"}, allEntries = true)
    public void evict() {
    }
}
