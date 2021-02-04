package io.kontur.insightsapi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumeratorsDenominatorsDto {

    private String xNumerator;

    private String xDenominator;

    private String xLabel;

    private String yNumerator;

    private String yDenominator;

    private String yLabel;

    private Double quality;
}
