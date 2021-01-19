package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticResolver implements GraphQLQueryResolver {

    private final StatisticService statisticService;

    public Statistic getAllStatistic(Integer defaultParam) {
        return statisticService.getAllStatistic();
    }

    public PolygonStatistic getPolygonStatistic(PolygonStatisticRequest request) {
        return statisticService.getPolygonStatistic();
    }
}
