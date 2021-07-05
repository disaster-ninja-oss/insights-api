package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import io.kontur.insightsapi.dto.StatisticDto;
import io.kontur.insightsapi.model.OsmQuality;
import io.kontur.insightsapi.model.Population;
import io.kontur.insightsapi.model.PopulationStatistic;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.PopulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PopulationResolver implements GraphQLResolver<PopulationStatistic> {

    private final PopulationService populationService;

    private final GeometryTransformer geometryTransformer;

    private final ObjectMapper objectMapper;

    public Population getPopulation(PopulationStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        String polygon = (String) environment.getExecutionStepInfo().getParent().getArguments().get("polygon");
        var transformedGeometry = geometryTransformer.transformToWkt(polygon);
        StatisticDto populationStatistic = populationService.calculatePopulation(transformedGeometry);
        return Population.builder()
                .population(populationStatistic.getPopulation())
                .gdp(populationStatistic.getGdp())
                .urban(populationStatistic.getUrban())
                .build();
    }

    public String getHumanitarianImpact(PopulationStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        String polygon = (String) environment.getExecutionStepInfo().getParent().getArguments().get("polygon");
        var transformedGeometry = geometryTransformer.transformToWkt(polygon);
        var impactDtos = populationService.calculateHumanitarianImpact(transformedGeometry);
        var collection = populationService.convertImpactIntoFeatureCollection(transformedGeometry, impactDtos);
        return objectMapper.writeValueAsString(collection);
    }

    public OsmQuality getOsmQuality(PopulationStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        var polygon = (String) environment.getExecutionStepInfo().getParent().getArguments().get("polygon");
        var transformedGeometry = geometryTransformer.transform(polygon);
        var fieldList = environment.getSelectionSet().getFields().stream()
                .map(SelectedField::getQualifiedName)
                .collect(Collectors.toList());
        return populationService.calculateOsmQuality(transformedGeometry, fieldList);
    }

}
