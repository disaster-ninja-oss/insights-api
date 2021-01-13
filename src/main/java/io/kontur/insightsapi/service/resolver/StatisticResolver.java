package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
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

    public Statistic getPolygonStatistic(PolygonStatisticRequest request) throws JsonProcessingException {
        return statisticService.getPolygonStatistic(request);
    }
}
