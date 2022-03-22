package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsQualitySortDto {

    private String numerator;

    private String denominator;

    private Double minQuality;
}
