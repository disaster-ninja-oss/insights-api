package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Meta;
import io.kontur.insightsapi.model.Statistic;
import org.springframework.stereotype.Component;

@Component
public class MetaResolver implements GraphQLResolver<Statistic> {

    public Meta getMeta(Statistic statistic) {
        return statistic.getMeta();
    }
}
