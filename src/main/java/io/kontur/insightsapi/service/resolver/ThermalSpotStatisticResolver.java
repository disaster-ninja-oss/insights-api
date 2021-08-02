package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.repository.ThermalSpotRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ThermalSpotStatisticResolver implements GraphQLResolver<PolygonStatistic> {

    private final GeometryTransformer geometryTransformer;

    private final ThermalSpotRepository thermalSpotRepository;

    public ThermalSpotStatistic getThermalSpotStatistic(PolygonStatistic statistic, DataFetchingEnvironment environment)
            throws JsonProcessingException {
        var arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getArguments().get("polygonStatisticRequest");
        var polygon = (String) arguments.get("polygon");
        var transformedGeometry = geometryTransformer.transform(polygon);
        var fieldList = environment.getSelectionSet().getFields().stream()
                .map(SelectedField::getQualifiedName)
                .collect(Collectors.toList());
        return thermalSpotRepository.calculateThermalSpotStatistic(transformedGeometry, fieldList);
    }
}
