package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class StatisticResolver implements GraphQLQueryResolver {

    private final StatisticService statisticService;

    public StatisticResolver(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    public Statistic getAllStatistic(Integer defaultParam) throws IOException {
        return statisticService.createObjFromJson();
    }
}
