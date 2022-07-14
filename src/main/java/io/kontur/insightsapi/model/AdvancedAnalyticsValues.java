package io.kontur.insightsapi.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsValues implements Serializable {

    private String calculation;

    private Double value;

    private Double quality;

}
