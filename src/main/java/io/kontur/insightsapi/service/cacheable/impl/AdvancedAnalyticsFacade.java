package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
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
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        return advancedAnalyticsCache.get(keyGen(argQuery, argGeometry),
                () -> repository.getAdvancedAnalytics(argQuery, argGeometry));
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
