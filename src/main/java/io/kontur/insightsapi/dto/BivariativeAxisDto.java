package io.kontur.insightsapi.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BivariativeAxisDto {

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

    private List<String> calculations;

    public BivariativeAxisDto(String numerator, String denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
}
