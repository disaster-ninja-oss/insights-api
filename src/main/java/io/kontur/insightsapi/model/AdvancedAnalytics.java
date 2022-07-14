package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AdvancedAnalytics implements Serializable {

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

    private List<AdvancedAnalyticsValues> analytics;

    private Integer order;

}