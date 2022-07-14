package io.kontur.insightsapi.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumeratorsDenominatorsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -5124660951574307864L;

    private String xNumerator;

    private String xDenominator;

    private String xLabel;

    private String yNumerator;

    private String yDenominator;

    private String yLabel;

    private Double quality;
}
