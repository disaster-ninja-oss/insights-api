package io.kontur.insightsapi.service.cacheable.impl;

import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.FunctionResult;
import io.kontur.insightsapi.repository.FunctionsRepository;
import io.kontur.insightsapi.service.cacheable.CacheEvictable;
import io.kontur.insightsapi.service.cacheable.FunctionsService;
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
@ConditionalOnProperty(prefix = "cache", name = "functions")
@RequiredArgsConstructor
public class FunctionsFacade implements FunctionsService, CacheEvictable {

    private final FunctionsRepository repository;

    @SneakyThrows
    @Override
    @Cacheable(value = "functions", keyGenerator = "stringListKeyGenerator")
    public List<FunctionResult> calculateFunctionsResult(String geojson, List<FunctionArgs> args) {
        return repository.calculateFunctionsResult(geojson, args);
    }

    @Override
    @CacheEvict(value = "functions", allEntries = true)
    public void evict() {
    }
}
