package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolygonStatisticRequest {

    private String polygon;

    @JsonProperty("xNumeratorList")
    private List<String> xNumeratorList;

    @JsonProperty("yNumeratorList")
    private List<String> yNumeratorList;
}
