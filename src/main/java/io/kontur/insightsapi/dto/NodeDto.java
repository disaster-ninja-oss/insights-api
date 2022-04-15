package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class NodeDto {

    private List<String> quotient;

    private String axis;
}
