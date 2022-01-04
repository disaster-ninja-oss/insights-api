package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ForAvgCorrelationDto {

    private Double sum;

    private Integer number;
}
