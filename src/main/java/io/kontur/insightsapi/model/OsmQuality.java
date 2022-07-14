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
public class OsmQuality implements Serializable {

    @Serial
    private static final long serialVersionUID = 8354932129780225721L;

    private Long peopleWithoutOsmBuildings;

    private BigDecimal areaWithoutOsmBuildingsKm2;

    private Long peopleWithoutOsmRoads;

    private BigDecimal areaWithoutOsmRoadsKm2;

    private Long peopleWithoutOsmObjects;

    private BigDecimal areaWithoutOsmObjectsKm2;

    private BigDecimal osmGapsPercentage;
}
