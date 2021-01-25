package io.kontur.insightsapi.dto;

import lombok.*;
import org.wololo.geojson.GeoJSON;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanitarianImpactDto {

    private BigDecimal population;

    private String percentage;

    private String name;

    private BigDecimal areaKm2;

    private GeoJSON geometry;

    private BigDecimal totalPopulation;

    private BigDecimal totalAreaKm2;

}
