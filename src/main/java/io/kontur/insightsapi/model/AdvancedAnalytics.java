package io.kontur.insightsapi.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class AdvancedAnalytics {

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

    private List<AdvancedAnalyticsValues> analytics;

}
