package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
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
    @Cacheable(value = "advanced-analytics-all", key = "'all'")
    public List<AdvancedAnalytics> getWorldData() {
        return repository.getWorldData();
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "advanced-analytics-all", keyGenerator = "listKeyGenerator")
    public List<AdvancedAnalytics> getFilteredWorldData(List<AdvancedAnalyticsRequest> argRequests) {
        return repository.getFilteredWorldData(argRequests);
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "advanced-analytics", keyGenerator = "stringStringKeyGenerator")
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        return repository.getAdvancedAnalytics(argQuery, argGeometry);
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "advanced-analytics", keyGenerator = "stringStringListKeyGenerator")
    public List<List<AdvancedAnalyticsValues>> getFilteredAdvancedAnalytics(String argQuery, String argGeometry, List<BivariativeAxisDto> axisDtos) {
        return repository.getFilteredAdvancedAnalytics(argQuery, argGeometry, axisDtos);
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "advanced-analytics-v2", keyGenerator = "listKeyGenerator")
    public List<AdvancedAnalytics> getAdvancedAnalyticsV2(List<BivariateIndicatorDto> indicators, String argGeometry) {
        return repository.getAdvancedAnalyticsV2(indicators, argGeometry);
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "advanced-analytics-v2", keyGenerator = "listKeyGenerator")
    public List<AdvancedAnalytics> getFilteredAdvancedAnalyticsV2(List<BivariateIndicatorDto> indicators, List<AdvancedAnalyticsRequest> argRequests, String argGeometry) {
        return repository.getFilteredAdvancedAnalyticsV2(indicators, argRequests, argGeometry);
    }

    @Override
    @CacheEvict(value = {"advanced-analytics-all", "advanced-analytics", "advanced-analytics-v2"}, allEntries = true)
    public void evict() {
    }
}
