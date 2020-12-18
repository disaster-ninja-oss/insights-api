package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.CorrelationRate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AxisCorrelationRateResolver implements GraphQLResolver<CorrelationRate> {

    public Axis getX(CorrelationRate correlationRate) {
        return correlationRate.getX();
    }

    public Axis getY(CorrelationRate correlationRate) {
        return correlationRate.getY();
    }
}
