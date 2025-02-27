package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.AxisInfo;
import io.kontur.insightsapi.model.TransformationInfo;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.repository.AxisRepository;
import io.kontur.insightsapi.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticResolver implements GraphQLQueryResolver {

    private final StatisticService statisticService;

    private final AxisRepository axisRepository;

    public Statistic getAllStatistic(Integer defaultParam) {
        return statisticService.getAllStatistic();
    }

    public PolygonStatistic getPolygonStatistic(PolygonStatisticRequest request) {
        return statisticService.getPolygonStatistic();
    }

    public AxisInfo getAxes() {
        return new AxisInfo(axisRepository.getAxes());
    }

    public TransformationInfo getTransformations(String numerator, String denominator) {
        return new TransformationInfo(axisRepository.getTransformations(numerator, denominator));
    }
}
