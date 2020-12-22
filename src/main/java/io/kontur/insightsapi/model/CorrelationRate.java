package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorrelationRate {

    private Axis x;

    private Axis y;

    private Float rate;

    private Float quality;

    private Float correlation;
}
