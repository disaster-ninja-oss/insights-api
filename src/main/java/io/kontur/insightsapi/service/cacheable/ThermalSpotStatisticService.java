package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.model.ThermalSpotStatistic;

import java.util.List;

public interface ThermalSpotStatisticService {

    ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList);
}
