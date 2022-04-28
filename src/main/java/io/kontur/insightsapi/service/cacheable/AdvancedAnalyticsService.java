package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;

import java.util.List;

public interface AdvancedAnalyticsService {

    List<AdvancedAnalytics> getWorldData();

    List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry);
}
