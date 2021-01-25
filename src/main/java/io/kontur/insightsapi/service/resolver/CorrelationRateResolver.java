package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CorrelationRateResolver implements GraphQLResolver<PolygonStatistic> {

    private final StatisticRepository statisticRepository;

    private final GeometryTransformer geometryTransformer;

    public List<PolygonCorrelationRate> getCorrelationRates(PolygonStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getArguments().get("polygonStatisticRequest");
        if (!arguments.containsKey("polygon")) {
            return statisticRepository.getAllCorrelationRateStatistics();
        }
        var transformedGeometry = geometryTransformer.transform((String) arguments.get("polygon"));
        if (!arguments.keySet().containsAll(List.of("xNumeratorList", "yNumeratorList"))) {
            return statisticRepository.getPolygonCorrelationRateStatistics(PolygonStatisticRequest.builder()
                    .polygon(transformedGeometry)
                    .build());
        }
        return statisticRepository.getPolygonNumeratorsCorrelationRateStatistics(PolygonStatisticRequest.builder()
                .polygon(transformedGeometry)
                .xNumeratorList((List<String>) arguments.get("xNumeratorList"))
                .yNumeratorList((List<String>) arguments.get("yNumeratorList"))
                .build());
    }
}
