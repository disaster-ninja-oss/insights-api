package io.kontur.insightsapi.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GraphDto {

    private Map<List<String>, Set<List<String>>> graph;

    private Map<List<String>, Double> xAvgCorrelation;

    private Map<List<String>, Double> yAvgCorrelation;
}
