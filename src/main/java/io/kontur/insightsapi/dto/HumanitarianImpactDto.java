package io.kontur.insightsapi.dto;

import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.math.BigDecimal;

@Getter
@Setter
public class HumanitarianImpactDto {

    private BigDecimal population;

    private String percentage;

    private String name;

    private BigDecimal areaKm2;

    private GeoJSON geometry;

    private BigDecimal totalPopulation;

    private BigDecimal totalAreaKm2;

}
