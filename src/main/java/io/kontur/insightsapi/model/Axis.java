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

    private List<String> parent;

    public Axis duplicate(){
        Axis result = new Axis();
        result.setQuality(this.quality);
        result.setSteps(this.steps);
        result.setQuotient(this.quotient);
        result.setLabel(this.label);
        result.setParent(this.parent);
        return result;
    }
}
