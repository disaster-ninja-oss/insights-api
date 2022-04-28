package io.kontur.insightsapi.service.cacheable.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.CorrelationRateService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@ConditionalOnProperty(prefix = "cache", name = "correlation-rate")
public class CorrelationRateFacade implements CorrelationRateService, CacheEvictable {

    private final StatisticRepository repository;

    private final Cache<String, List<PolygonCorrelationRate>> allCorrelationRateStatisticsCache;

    private final Cache<String, List<NumeratorsDenominatorsDto>> numeratorsDenominatorsForCorrelationCache;

    private final Cache<String, List<Double>> polygonCorrelationRateStatisticsBatchCache;

    private final Cache<String, Map<String, Boolean>> numeratorsForNotEmptyLayersBatchCache;

    private final HashFunction hashFunction;

    public CorrelationRateFacade(StatisticRepository repository, @Value("${cache.maximumSize}") Integer maximumSize) {
        this.repository = repository;
        this.allCorrelationRateStatisticsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.numeratorsDenominatorsForCorrelationCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.polygonCorrelationRateStatisticsBatchCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.numeratorsForNotEmptyLayersBatchCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build();
        this.hashFunction = Hashing.murmur3_32_fixed();
    }

    @SneakyThrows
    @Override
    public List<PolygonCorrelationRate> getAllCorrelationRateStatistics() {
        return allCorrelationRateStatisticsCache.get("all",
                repository::getAllCorrelationRateStatistics);
    }

    @SneakyThrows
    @Override
    public List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation() {
        return numeratorsDenominatorsForCorrelationCache.get("all",
                repository::getNumeratorsDenominatorsForCorrelation);
    }

    @SneakyThrows
    @Override
    public List<Double> getPolygonCorrelationRateStatisticsBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon) {
        return polygonCorrelationRateStatisticsBatchCache.get(keyGen(dtoList, polygon),
                () -> repository.getPolygonCorrelationRateStatisticsBatch(dtoList, polygon));
    }

    @SneakyThrows
    @Override
    public Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon) {
        return numeratorsForNotEmptyLayersBatchCache.get(keyGen(dtoList, polygon),
                () -> repository.getNumeratorsForNotEmptyLayersBatch(dtoList, polygon));
    }

    private String keyGen(List<NumeratorsDenominatorsDto> dtoList, String geojson) {
        return hashFunction.hashString(geojson, Charset.defaultCharset()) + "_"
                + hashFunction.hashString(dtoList.toString(), Charset.defaultCharset());
    }

    @Override
    public void evict() {
        allCorrelationRateStatisticsCache.invalidateAll();
        numeratorsDenominatorsForCorrelationCache.invalidateAll();
        polygonCorrelationRateStatisticsBatchCache.invalidateAll();
        numeratorsForNotEmptyLayersBatchCache.invalidateAll();
    }
}
