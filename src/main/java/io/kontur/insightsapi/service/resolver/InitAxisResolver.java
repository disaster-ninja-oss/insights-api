package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.InitAxis;
import io.kontur.insightsapi.model.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitAxisResolver implements GraphQLResolver<Statistic> {

    public InitAxis getInitAxis(Statistic statistic){
        return statistic.getInitAxis();
    }
}
