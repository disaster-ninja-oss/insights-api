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
public class UrbanCore implements Serializable {

    @Serial
    private static final long serialVersionUID = 3703114307999395078L;

    private BigDecimal urbanCorePopulation;

    private BigDecimal urbanCoreAreaKm2;

    private BigDecimal totalPopulatedAreaKm2;
}
