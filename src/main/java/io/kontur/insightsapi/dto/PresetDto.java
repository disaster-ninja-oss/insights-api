package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class PresetDto {

    @NotEmpty(message = "x_numerator_id cannot be null or empty")
    private String x_numerator_id;

    @NotEmpty(message = "x_denominator_id cannot be null or empty")
    private String x_denominator_id;

    @NotEmpty(message = "y_numerator_id cannot be null or empty")
    private String y_numerator_id;

    @NotEmpty(message = "y_denominator_id cannot be null or empty")
    private String y_denominator_id;

    private Double ord;

    private String name;

    private String description;

    private String colors;

    private String application;

    private Boolean active;

    private Boolean is_public;
}
