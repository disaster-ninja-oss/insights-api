package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class PureNumeratorDenominatorDto {

    private String numerator;

    private String denominator;
}
