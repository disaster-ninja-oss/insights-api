package io.kontur.insightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsRequest {

    private String numerator;

    private String denominator;

    private List<String> calculations;

}
