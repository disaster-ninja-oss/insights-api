package io.kontur.insightsapi.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThermalSpotStatistic implements Serializable {

    @Serial
    private static final long serialVersionUID = 6979650093124229059L;

    private BigDecimal industrialAreaKm2;

    private Long hotspotDaysPerYearMax;

    private Long volcanoesCount;

    private BigDecimal forestAreaKm2;

}
