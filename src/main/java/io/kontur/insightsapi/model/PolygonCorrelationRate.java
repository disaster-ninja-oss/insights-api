package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class PolygonCorrelationRate extends CorrelationRate implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 3252610813500507339L;

    @Override
    public PolygonCorrelationRate clone() {
        PolygonCorrelationRate result = new PolygonCorrelationRate();
        try {
            result = (PolygonCorrelationRate) super.clone();
        } catch (CloneNotSupportedException e) {
            result.setCorrelation(getCorrelation());
            result.setAvgCorrelationX(getAvgCorrelationX());
            result.setAvgCorrelationY(getAvgCorrelationY());
            result.setRate(getRate());
            result.setQuality(getQuality());
        }
        result.setX(getX().clone());
        result.setY(getY().clone());
        return result;
    }
}
