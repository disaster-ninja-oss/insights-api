package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class AxisOverridesRequest {

    @NotEmpty(message = "Numerator id cannot be null or empty")
    private String numerator_id;

    @NotEmpty(message = "Denominator id cannot be null or empty")
    private String denominator_id;

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
