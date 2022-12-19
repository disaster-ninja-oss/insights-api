package io.kontur.insightsapi.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NumeratorsDenominatorsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -5124660952574304864L;

    private String xNumerator;

    private String xDenominator;

    private String xLabel;

    private UUID xNumUuid;

    private UUID xDenUuid;

    private String yNumerator;

    private String yDenominator;

    private String yLabel;

    private UUID yNumUuid;

    private UUID yDenUuid;

    private Double quality;
}
