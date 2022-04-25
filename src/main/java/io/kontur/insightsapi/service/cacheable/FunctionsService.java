package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.FunctionResult;

import java.util.List;

public interface FunctionsService {

    List<FunctionResult> calculateFunctionsResult(String geojson, List<FunctionArgs> args);
}
