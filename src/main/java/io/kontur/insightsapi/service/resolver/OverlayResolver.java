package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Color;
import io.kontur.insightsapi.model.Overlay;
import io.kontur.insightsapi.model.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverlayResolver implements GraphQLResolver<Statistic> {

    public List<Overlay> getOverlays(Statistic statistic) {
        return statistic.getOverlays();
    }
}
