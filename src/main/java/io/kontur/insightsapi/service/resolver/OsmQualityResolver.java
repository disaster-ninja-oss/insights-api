package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.model.OsmQuality;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.cacheable.OsmQualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OsmQualityResolver implements GraphQLResolver<Analytics> {

    private final GeometryTransformer geometryTransformer;

    private final Helper helper;

    private final OsmQualityService osmQualityService;

    public OsmQuality getOsmQuality(Analytics analytics, DataFetchingEnvironment environment) throws JsonProcessingException {
        final Boolean GEOMETRY_CAN_BE_NULL = false;
        var polygon = helper.getPolygonFromRequest(environment);
        var transformedGeometry = geometryTransformer.transform(polygon, GEOMETRY_CAN_BE_NULL);
        var fieldList = environment.getSelectionSet().getFields().stream()
                .map(SelectedField::getQualifiedName)
                .collect(Collectors.toList());
        return osmQualityService.calculateOsmQuality(transformedGeometry, fieldList);
    }
}
