package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NodeDto {

    private List<String> quotient;

    private Double avgCorrelationX;

    private Double avgCorrelationY;
}
