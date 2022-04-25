package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.model.UrbanCore;

import java.util.List;

public interface UrbanCoreService {

    UrbanCore calculateUrbanCore(String geojson, List<String> requestFields);
}
