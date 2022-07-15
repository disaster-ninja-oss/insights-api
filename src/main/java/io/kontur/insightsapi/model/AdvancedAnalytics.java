package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AdvancedAnalytics implements Serializable {

    @Serial
    private static final long serialVersionUID = 8438781415752740197L;

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

    private List<AdvancedAnalyticsValues> analytics;

    private Integer order;

}