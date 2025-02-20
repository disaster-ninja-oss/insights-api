package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.repository.AdvancedAnalyticsRepository;
import io.kontur.insightsapi.repository.IndicatorRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdvancedAnalyticsResolver implements GraphQLResolver<Analytics> {

    private final Helper helper;

    private final GeometryTransformer geometryTransformer;

    private final AdvancedAnalyticsRepository advancedAnalyticsRepository;

    private final IndicatorRepository indicatorRepository;

    private final AdvancedAnalyticsService advancedAnalyticsService;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsResolver.class);

    public List<AdvancedAnalytics> getAdvancedAnalytics(Analytics statistic, List<AdvancedAnalyticsRequest> argRequests, DataFetchingEnvironment environment) throws JsonProcessingException {
        var polygon = helper.getPolygonFromRequest(environment);
        if (polygon != null) {
            var transformedGeometry = geometryTransformer.transform(polygon, true);
            if (transformedGeometry != null) {
                if (argRequests != null && !argRequests.isEmpty()) {
                    return getFilteredAdvancedAnalyticsV2(argRequests, helper.transformGeometryToWkt(transformedGeometry));
                } else {
                    return getAdvancedAnalyticsV2(helper.transformGeometryToWkt(transformedGeometry));
                }
            } else {
                return getWorldData(argRequests);
            }
        } else {
            return getWorldData(argRequests);
        }
    }

    private List<AdvancedAnalytics> getAdvancedAnalyticsV2(String transformedGeometryAsWkt) {
        List<BivariateIndicatorDto> indicators = indicatorRepository.getAllBivariateIndicators();

        List<AdvancedAnalytics> unsortedResultList = advancedAnalyticsService.getAdvancedAnalyticsV2(transformedGeometryAsWkt, indicators);
        return advancedAnalyticsRepository.sortResultList(unsortedResultList);
    }

    private List<AdvancedAnalytics> getFilteredAdvancedAnalyticsV2(List<AdvancedAnalyticsRequest> argRequests, String transformedGeometryAsWkt) {
        List<BivariateIndicatorDto> indicators = indicatorRepository.getAllBivariateIndicators();
        List<BivariativeAxisDto> axisDtos = createAxisDtosFromRequest(argRequests);

        List<AdvancedAnalytics> unsortedResultList = advancedAnalyticsService.getFilteredAdvancedAnalyticsV2(transformedGeometryAsWkt, indicators, axisDtos);
        return advancedAnalyticsRepository.sortResultList(unsortedResultList);
    }

    private List<BivariativeAxisDto> createAxisDtosFromRequest(List<AdvancedAnalyticsRequest> argRequests) {
        List<BivariativeAxisDto> axisDtos = advancedAnalyticsRepository.getFilteredBivariativeAxis(argRequests);
        if (!axisDtos.isEmpty()) {
            axisDtos.forEach(a -> {
                AdvancedAnalyticsRequest request = argRequests.stream().filter(r -> r.getNumerator().equals(a.getNumerator()) && r.getDenominator().equals(a.getDenominator()))
                        .findFirst().orElse(null);
                if (request != null) {
                    a.setCalculations(request.getCalculations());
                }
            });
        }
        return axisDtos;
    }

    private List<AdvancedAnalytics> getWorldData(List<AdvancedAnalyticsRequest> argRequest) {
        if (argRequest != null && !argRequest.isEmpty()) {
            return advancedAnalyticsService.getFilteredWorldData(argRequest);
        } else {
            return advancedAnalyticsService.getWorldData();
        }

    }
}
