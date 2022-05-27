package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "advanced-analytics")
public class AdvancedAnalyticsFacade implements AdvancedAnalyticsService, CacheEvictable {

    private final AdvancedAnalyticsRepository repository;

    private final Cache<String, List<AdvancedAnalytics>> worldCache;

    private final Cache<String, List<List<AdvancedAnalyticsValues>>> advancedAnalyticsCache;

    private final HashFunction hashFunction;

    public AdvancedAnalyticsFacade(AdvancedAnalyticsRepository repository, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.repository = repository;
        this.worldCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.advancedAnalyticsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public List<AdvancedAnalytics> getWorldData() {
        return worldCache.get("all", repository::getWorldData);
    }

    @SneakyThrows
    @Override
    public List<AdvancedAnalytics> getFilteredWorldData(List<AdvancedAnalyticsRequest> argRequests) {
        List<String> sortedFilterList = argRequests.stream()
                .sorted(Comparator.comparing(AdvancedAnalyticsRequest::getNumerator).thenComparing(AdvancedAnalyticsRequest::getDenominator))
                .map(r -> r.getNumerator() + r.getDenominator() + StringUtils.join(
                        r.getCalculations().stream().sorted().toList()))
                .toList();
        return worldCache.get(StringUtils.join(sortedFilterList), () -> repository.getFilteredWorldData(argRequests));
    }

    @SneakyThrows
    @Override
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        return advancedAnalyticsCache.get(keyGen(argQuery, argGeometry),
                () -> repository.getAdvancedAnalytics(argQuery, argGeometry));
    }

    @SneakyThrows
    @Override
    public List<List<AdvancedAnalyticsValues>> getFilteredAdvancedAnalytics(String argQuery, String argGeometry, List<BivariativeAxisDto> axisDtos) {
        List<String> sortedFilterList = axisDtos.stream()
                .sorted(Comparator.comparing(BivariativeAxisDto::getNumerator).thenComparing(BivariativeAxisDto::getDenominator))
                .map(r -> r.getNumerator() + r.getDenominator() + StringUtils.join(
                        r.getCalculations().stream().sorted().toList()
                ))
                .toList();
        return advancedAnalyticsCache.get(keyGen(StringUtils.join(sortedFilterList), argGeometry),
                () -> repository.getFilteredAdvancedAnalytics(argQuery, argGeometry, axisDtos));
    }

    private String keyGen(String argQuery, String geojson) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()) + "_"
                + hashFunction.hashString(argQuery, Charset.defaultCharset());
    }

    @Override
    public void evict() {
        worldCache.invalidateAll();
        advancedAnalyticsCache.invalidateAll();
    }
}
