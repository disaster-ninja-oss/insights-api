package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NodeDto {

    private List<String> quotient;

    private Double avgCorrelationX;

    private Double avgCorrelationY;
}
