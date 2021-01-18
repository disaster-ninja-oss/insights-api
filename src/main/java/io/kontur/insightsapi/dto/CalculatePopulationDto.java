package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CalculatePopulationDto {

    private BigDecimal population;

    private BigDecimal urban;

    private BigDecimal gdp;

    private String type;
}
