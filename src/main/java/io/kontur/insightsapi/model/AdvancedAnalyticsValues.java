package io.kontur.insightsapi.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsValues {

    private String calculation;

    private Double value;

    private Double quality;

}
