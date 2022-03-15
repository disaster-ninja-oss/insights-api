package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdvancedAnalyticsResolver implements GraphQLResolver<Analytics> {

    private final Helper helper;

    private final GeometryTransformer geometryTransformer;

    private final AdvancedAnalyticsRepository advancedAnalyticsRepository;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsResolver.class);

    public List<AdvancedAnalytics> getAdvancedAnalytics(Analytics statistic, DataFetchingEnvironment environment) throws IOException {
        var polygon = helper.getPolygonFromRequest(environment);
        if (polygon != null) {
            var transformedGeometry = geometryTransformer.transform(polygon);

            //got bivariative axis, will be parametric, not all list
            List<BivariativeAxisDto> axisDtos = advancedAnalyticsRepository.getBivariativeAxis();

            //query with geom and uniun of bivariative axis caculations
            String queryWithGeom = advancedAnalyticsRepository.getQueryWithGeom(axisDtos);
            String queryUnionAll = StringUtils.join(axisDtos.stream().map(advancedAnalyticsRepository::getUnionQuery).collect(Collectors.toList()), " union all ");

            //get analytics result and match layer names
            var advancedAnalyticsValues = advancedAnalyticsRepository.getAdvancedAnalytics(queryWithGeom + " " + queryUnionAll, transformedGeometry);

            return getAdvancedAnalyticsResult(axisDtos, advancedAnalyticsValues);
        } else {
            return advancedAnalyticsRepository.getWorldData();
        }
    }

    private List<AdvancedAnalytics> getAdvancedAnalyticsResult(List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues) {
        List<AdvancedAnalytics> returnList = new ArrayList<>();
        for (int i = 0; i < argAxis.size(); i++) {
            AdvancedAnalytics analytics = new AdvancedAnalytics();
            analytics.setNumerator(argAxis.get(i).getNumerator());
            analytics.setDenominator(argAxis.get(i).getDenominator());
            analytics.setNumeratorLabel(argAxis.get(i).getNumeratorLabel());
            analytics.setDenominatorLabel(argAxis.get(i).getDenominatorLabel());
            analytics.setAnalytics(argValues.get(i));
            returnList.add(analytics);
        }
        return returnList;
    }
}
