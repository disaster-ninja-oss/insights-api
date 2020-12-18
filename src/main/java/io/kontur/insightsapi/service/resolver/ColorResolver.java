package io.kontur.insightsapi.service.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import io.kontur.insightsapi.model.Color;
import io.kontur.insightsapi.model.Overlay;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ColorResolver implements GraphQLResolver<Overlay> {

    public List<Color> getColor(Overlay overlay){
        return overlay.getColor();
    }
}
