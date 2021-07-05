package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Population {

    private BigDecimal population;

    private BigDecimal urban;

    private BigDecimal gdp;
}
