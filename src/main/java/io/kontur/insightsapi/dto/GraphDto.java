package io.kontur.insightsapi.dto;

import lombok.*;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GraphDto {

    private Map<NodeDto, Set<NodeDto>> graph;

    private Map<NodeDto, Double> xAvgCorrelation;

    private Map<NodeDto, Double> yAvgCorrelation;
}
