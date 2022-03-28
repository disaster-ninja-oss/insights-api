package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.model.OsmQuality;

import java.util.List;

public interface OsmQualityService {

    OsmQuality calculateOsmQuality(String geojson, List<String> osmRequestFields);
}
