package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.StatisticDto;

public interface PopulationService {

    StatisticDto calculatePopulation(String geojson);
}
