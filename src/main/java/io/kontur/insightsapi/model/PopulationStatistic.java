package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PopulationStatistic {

    private Population population;

    private String humanitarianImpact;

    private OsmQuality osmQuality;
}
