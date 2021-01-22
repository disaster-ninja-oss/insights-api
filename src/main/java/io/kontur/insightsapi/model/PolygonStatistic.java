package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PolygonStatistic extends BaseStatistic {

    private List<PolygonCorrelationRate> correlationRates;
}
