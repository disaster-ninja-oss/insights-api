package io.kontur.insightsapi.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsmQuality {

    private Long peopleWithoutOsmBuildings;

    private BigDecimal areaWithoutOsmBuildingsKm2;

    private Long peopleWithoutOsmRoads;

    private BigDecimal areaWithoutOsmRoadsKm2;

    private Long peopleWithoutOsmObjects;

    private BigDecimal areaWithoutOsmObjectsKm2;

    private BigDecimal osmGapsPercentage;
}
