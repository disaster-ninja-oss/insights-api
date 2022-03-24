package io.kontur.insightsapi.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolygonStatisticRequest {

    private String polygon;

    private String polygonV2;

    private List<List<String>> importantLayers;
}
