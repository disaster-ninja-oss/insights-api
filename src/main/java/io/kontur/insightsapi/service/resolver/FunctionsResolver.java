package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.model.Functions;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FunctionsResolver implements GraphQLResolver<Analytics> {

    private final GeometryTransformer geometryTransformer;

    private final Helper helper;

    public Functions getFunctions(Analytics analytics, DataFetchingEnvironment environment) throws JsonProcessingException {
        var polygon = helper.getPolygonFromRequest(environment);
        var transformedGeometry = geometryTransformer.transform(polygon);
        //var fieldMap = environment.getSelectionSet().getFields().stream() - <fieldName, function args>
        return null;
    }
}
