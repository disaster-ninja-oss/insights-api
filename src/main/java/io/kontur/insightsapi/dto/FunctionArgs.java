package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FunctionArgs {

    private String id;

    private String name;

    private String x;

    private String y;
}
