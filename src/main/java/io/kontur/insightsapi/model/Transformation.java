package io.kontur.insightsapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transformation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1717416701010101010L;

    private String transformation;

    private Double min;

    private Double mean;

    private Double skew;

    private Double stddev;

    private Double lowerBound;

    private Double upperBound;

    private List<Double> points;

}
