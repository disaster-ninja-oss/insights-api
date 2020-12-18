package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.InitAxis;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AxisInitAxisResolver implements GraphQLResolver<InitAxis> {

    public Axis getX(InitAxis initAxis) {
        return initAxis.getX();
    }

    public Axis getY(InitAxis initAxis) {
        return initAxis.getY();
    }
}
