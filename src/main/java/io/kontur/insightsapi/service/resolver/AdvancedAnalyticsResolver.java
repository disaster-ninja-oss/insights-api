package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.AdvancedAnalyticsQualitySortDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdvancedAnalyticsResolver implements GraphQLResolver<Analytics> {

    private final Helper helper;

    private final GeometryTransformer geometryTransformer;

    private final AdvancedAnalyticsRepository advancedAnalyticsRepository;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsResolver.class);

    public List<AdvancedAnalytics> getAdvancedAnalytics(Analytics statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
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

            //list need to be sorted according to any least quality value
            List<AdvancedAnalyticsQualitySortDto> qualitySortedList = advancedAnalyticsRepository.createSortedList(axisDtos, advancedAnalyticsValues);
            return advancedAnalyticsRepository.getAdvancedAnalyticsResult(qualitySortedList, axisDtos, advancedAnalyticsValues);
        } else {
            return advancedAnalyticsRepository.getWorldData();
        }
    }
}