package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.repository.ThermalSpotRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.ThermalSpotStatisticService;
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
@ConditionalOnProperty(prefix = "cache", name = "thermal-spot")
public class ThermalSpotStatisticFacade implements ThermalSpotStatisticService, CacheEvictable {

    private final ThermalSpotRepository repository;

    private final Cache<String, ThermalSpotStatistic> cache;

    private final HashFunction hashFunction;

    public ThermalSpotStatisticFacade(ThermalSpotRepository repository, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.repository = repository;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList) {
        return cache.get(keyGen(geojson, fieldList),
                () -> repository.calculateThermalSpotStatistic(geojson, fieldList));
    }

    private String keyGen(String geojson, List<String> fieldList) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()) + "_"
                + hashFunction.hashString(fieldList.toString(), Charset.defaultCharset());
    }

    @Override
    public void evict() {
        cache.invalidateAll();
    }
}
