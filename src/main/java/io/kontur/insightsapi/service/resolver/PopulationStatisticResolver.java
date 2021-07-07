package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLResolver;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.model.PopulationStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopulationStatisticResolver implements GraphQLResolver<PolygonStatistic> {

    public PopulationStatistic getPopulationStatistic(PolygonStatistic statistic){
        return new PopulationStatistic();
    }
}
