package io.kontur.insightsapi.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionArgs {

    private String name;

    private String x;

    private String y;
}
