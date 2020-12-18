package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.Overlay;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AxisOverlayResolver implements GraphQLResolver<Overlay> {

    public Axis getX(Overlay overlay) {
        return overlay.getX();
    }

    public Axis getY(Overlay overlay) {
        return overlay.getY();
    }
}
