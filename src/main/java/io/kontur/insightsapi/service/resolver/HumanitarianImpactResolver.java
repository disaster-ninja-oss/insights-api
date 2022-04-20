package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.PopulationTransformer;
import io.kontur.insightsapi.service.cacheable.HumanitarianImpactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HumanitarianImpactResolver implements GraphQLResolver<Analytics> {

    private final PopulationTransformer populationTransformer;

    private final GeometryTransformer geometryTransformer;

    private final ObjectMapper objectMapper;

    private final Helper helper;

    private final HumanitarianImpactService humanitarianImpactService;

    public String getHumanitarianImpact(Analytics analytics, DataFetchingEnvironment environment) throws JsonProcessingException {
        var polygon = helper.getPolygonFromRequest(environment);
        var transformedGeometry = geometryTransformer.transform(polygon);
        var impactDtos = humanitarianImpactService.calculateHumanitarianImpact(transformedGeometry);
        var collection = populationTransformer.convertImpactIntoFeatureCollection(transformedGeometry, impactDtos);
        return objectMapper.writeValueAsString(collection);
    }
}
