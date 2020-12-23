package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Statistic {

    private List<Axis> axis;

    private Meta meta;

    private InitAxis initAxis;

    private List<Overlay> overlays;

    private List<CorrelationRate> correlationRates;
}
