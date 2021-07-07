package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLResolver;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BivariateStatisticResolver implements GraphQLResolver<PolygonStatistic> {

    private final StatisticService statisticService;

    public BivariateStatistic getBivariateStatistic(PolygonStatistic statistic) {
        return statisticService.getBivariateStatistic();
    }
}
