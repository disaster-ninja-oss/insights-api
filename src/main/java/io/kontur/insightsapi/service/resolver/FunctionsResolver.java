package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.model.FunctionResult;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.cacheable.FunctionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FunctionsResolver implements GraphQLResolver<Analytics> {

    private final GeometryTransformer geometryTransformer;

    private final Helper helper;

    private final FunctionsService functionsService;

    public List<FunctionResult> getFunctions(Analytics analytics, List<FunctionArgs> args, DataFetchingEnvironment environment) throws JsonProcessingException {
        final Boolean GEOMETRY_CAN_BE_NULL = false;
        var polygon = helper.getPolygonFromRequest(environment);
        var transformedGeometry = geometryTransformer.transform(polygon, GEOMETRY_CAN_BE_NULL);
        return functionsService.calculateFunctionsResult(transformedGeometry, args);
    }
}
