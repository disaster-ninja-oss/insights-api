package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolygonStatistic {

    private BivariateStatistic bivariateStatistic;

    private PopulationStatistic populationStatistic;
}
