package io.kontur.insightsapi.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThermalSpotStatistic implements Serializable {

    private BigDecimal industrialAreaKm2;

    private Long hotspotDaysPerYearMax;

    private Long volcanoesCount;

    private BigDecimal forestAreaKm2;

}
