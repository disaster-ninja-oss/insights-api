package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AxisOverridesRequest {

    @NotNull(message = "Numerator cannot be null or empty")
    private String numerator;

    @NotNull(message = "Denominator cannot be null or empty")
    private String denominator;

    private String label;

    private double min;

    private double max;

    private double p25;

    private double p75;
}
