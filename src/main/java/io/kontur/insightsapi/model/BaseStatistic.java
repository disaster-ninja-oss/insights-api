package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BaseStatistic {

    private List<Axis> axis;

    private Meta meta;

    private InitAxis initAxis;

    private List<Overlay> overlays;

    private List<Indicator> indicators;

    private Color colors;
}
