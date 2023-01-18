package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;

import java.util.List;

public interface AdvancedAnalyticsService {

    List<AdvancedAnalytics> getWorldData();

    List<AdvancedAnalytics> getFilteredWorldData(List<AdvancedAnalyticsRequest> argRequests);

    List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry);

    List<List<AdvancedAnalyticsValues>> getFilteredAdvancedAnalytics(String argQuery, String argGeometry, List<BivariativeAxisDto> axisDtos);

    List<AdvancedAnalytics> getAdvancedAnalyticsV2(List<BivariateIndicatorDto> axisDtos, String argGeometry);

    List<AdvancedAnalytics> getFilteredAdvancedAnalyticsV2(List<BivariateIndicatorDto> indicators, List<AdvancedAnalyticsRequest> argRequests, String argGeometry);

}
