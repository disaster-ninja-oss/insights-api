package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.model.PolygonCorrelationRate;

import java.util.List;
import java.util.Map;

public interface CorrelationRateService {

    List<PolygonCorrelationRate> getAllCorrelationRateStatistics();

    List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation();

    List<Double> getPolygonCorrelationRateStatisticsBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon);

    Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon);
}
