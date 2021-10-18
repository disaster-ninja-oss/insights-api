package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Functions {

    private BigDecimal population;

    private BigDecimal settledArea;

    private BigDecimal peopleWithoutOsmObjects;

    private BigDecimal settledAreaWithoutOsmObjects;

    private BigDecimal settledAreaWithoutOsmBuildingsPercent;

    private BigDecimal peopleWithoutOsmBuildings;

    private BigDecimal settledAreaWithoutOsmBuildings;

    private BigDecimal settledAreaWithoutOsmRoadsPercent;

    private BigDecimal peopleWithoutOsmRoads;

    private BigDecimal settledAreaWithoutOsmRoads;
}
