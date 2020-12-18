package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AxisStatisticResolver implements GraphQLResolver<Statistic> {

    public List<Axis> getAxis(Statistic statistic) {
        return statistic.getAxis();
    }
}
