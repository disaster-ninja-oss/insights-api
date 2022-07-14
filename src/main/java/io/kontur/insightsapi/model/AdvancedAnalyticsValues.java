package io.kontur.insightsapi.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsValues implements Serializable {

    @Serial
    private static final long serialVersionUID = 2143991954934684412L;

    private String calculation;

    private Double value;

    private Double quality;

}
