package io.kontur.insightsapi.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Axis {

    private String label;

    private List<Step> steps;

    private Double quality;

    private List<String> quotient;
}
