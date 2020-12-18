package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.Step;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StepResolver implements GraphQLResolver<Axis> {

    public List<Step> getSteps(Axis axis){
        return axis.getSteps();
    }
}
