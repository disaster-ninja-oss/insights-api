package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolygonCorrelationRate extends CorrelationRate {

    public PolygonCorrelationRate duplicate() {
        PolygonCorrelationRate result = new PolygonCorrelationRate();
        result.setCorrelation(getCorrelation());
        result.setAvgCorrelationX(getAvgCorrelationX());
        result.setAvgCorrelationY(getAvgCorrelationY());
        result.setRate(getRate());
        result.setQuality(getQuality());
        Axis x = getX().duplicate();
        result.setX(x);
        Axis y = getY().duplicate();
        result.setY(y);
        return result;
    }
}
