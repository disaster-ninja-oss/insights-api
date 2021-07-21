package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThermalSpotStatistic {

    private BigDecimal industrialAreaKm2;

    private Long hotspotDaysPerYearMax;

    private Long volcanoesCount;

    private BigDecimal forestAreaKm2;

}
