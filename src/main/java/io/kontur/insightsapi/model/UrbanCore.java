package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrbanCore {

    private BigDecimal urbanCorePopulation;

    private BigDecimal urbanCoreAreaKm2;

    private BigDecimal totalPopulatedAreaKm2;
}
