package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.CorrelationRate;
import io.kontur.insightsapi.model.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorrelationRateResolver implements GraphQLResolver<Statistic> {

    public List<CorrelationRate> getCorrelationRates(Statistic statistic) {
        return statistic.getCorrelationRates();
    }
}
