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

    private String numerator_uuid;

    private String denominator;

    private String denominator_uuid;

    private String owner;

    private String numeratorLabel;

    private String denominatorLabel;

    private double min;

    private double percentile_33;

    private double percentile_66;

    private double max;

    private double quality;

    private List<String> calculations;

    public BivariativeAxisDto(String numerator, String numerator_uuid, String denominator, String denominator_uuid, String owner) {
        this.numerator = numerator;
        this.numerator_uuid = numerator_uuid;
        this.denominator = denominator;
        this.denominator_uuid = denominator_uuid;
        this.owner = owner;
    }
}
