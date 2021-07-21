package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThermalSpotStatistic {

    private BigDecimal industrialArea;

    private Long wildfires;

    private Long volcanoesCount;

    private BigDecimal forestAreaKm2;

}
