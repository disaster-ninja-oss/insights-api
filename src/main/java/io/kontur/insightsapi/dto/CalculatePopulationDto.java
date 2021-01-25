package io.kontur.insightsapi.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatePopulationDto {

    private BigDecimal population;

    private BigDecimal urban;

    private BigDecimal gdp;

    private String type;
}
