package io.kontur.insightsapi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BivariativeAxisDto {

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

}
