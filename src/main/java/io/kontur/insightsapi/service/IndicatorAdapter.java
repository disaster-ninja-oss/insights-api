package io.kontur.insightsapi.service;

import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IndicatorAdapter {
    private final IndicatorRepository indicatorRepository;

}
