package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CorrelationRate implements Serializable {

    @Serial
    private static final long serialVersionUID = -5772207819296054714L;

    private Axis x;

    private Axis y;

    private Double rate;

    private Double quality;

    private Double correlation;

    private Double avgCorrelationX;

    private Double avgCorrelationY;
}
