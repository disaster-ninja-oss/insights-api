package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PolygonStatisticRequest {

    private String polygon;

    @JsonProperty("xDenominatorList")
    private List<String> xDenominatorList;

    @JsonProperty("yDenominatorList")
    private List<String> yDenominatorList;

    @JsonProperty("xNumeratorList")
    private List<String> xNumeratorList;

    @JsonProperty("yNumeratorList")
    private List<String> yNumeratorList;
}
