package io.kontur.insightsapi.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdvancedAnalyticsRequest {

    private String numerator;

    private String denominator;

    private List<String> calculations;

}
