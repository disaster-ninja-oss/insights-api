package io.kontur.insightsapi.service.resolver;

import graphql.kickstart.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Analytics;
import io.kontur.insightsapi.model.PolygonStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsResolver implements GraphQLResolver<PolygonStatistic> {

    public Analytics getAnalytics(PolygonStatistic statistic){
        return new Analytics();
    }
}
