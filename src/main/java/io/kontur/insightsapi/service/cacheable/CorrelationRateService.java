package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.model.PolygonCorrelationRate;

import java.util.List;
import java.util.Map;

public interface CorrelationRateService {

    List<PolygonCorrelationRate> getAllCorrelationRateStatistics();

    List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation();

    List<Double> getPolygonCorrelationRateStatisticsBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList);

    Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList);
}
