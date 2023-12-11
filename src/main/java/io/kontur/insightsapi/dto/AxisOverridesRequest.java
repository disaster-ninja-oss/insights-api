package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AxisOverridesRequest {

    @NotNull(message = "Numerator uuid cannot be null or empty")
    private String numerator_uuid;

    @NotNull(message = "Denominator uuid cannot be null or empty")
    private String denominator_uuid;

    private String label;

    private Double min;

    private Double max;

    private Double p25;

    private Double p75;

    private String minLabel;

    private String maxLabel;

    private String p25Label;

    private String p75Label;

}
