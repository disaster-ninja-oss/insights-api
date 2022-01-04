package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorrelationRate {

    private Axis x;

    private Axis y;

    private Double rate;

    private Double quality;

    private Double correlation;

    private Double avgCorrelationX;

    private Double avgCorrelationY;
}
