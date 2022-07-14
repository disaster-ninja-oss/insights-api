package io.kontur.insightsapi.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumeratorsDenominatorsDto implements Serializable {

    private String xNumerator;

    private String xDenominator;

    private String xLabel;

    private String yNumerator;

    private String yDenominator;

    private String yLabel;

    private Double quality;
}
