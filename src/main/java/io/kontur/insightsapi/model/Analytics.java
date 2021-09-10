package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Analytics {

    private Population population;

    private String humanitarianImpact;

    private ThermalSpotStatistic thermalSpotStatistic;

    private OsmQuality osmQuality;

    private UrbanCore urbanCore;
}
