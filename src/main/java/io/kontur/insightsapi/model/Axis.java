package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Axis {

    private String label;

    private List<Step> steps;

    private Double quality;

    private List<String> quotient;
}
