package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AxisResolver implements GraphQLResolver<PolygonStatistic> {

    private final StatisticRepository statisticRepository;

    public List<Axis> getAxis(PolygonStatistic statistic){
        return statisticRepository.getAxisStatistic();
    }
}
