package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(content = JsonInclude.Include.NON_NULL)
public class StatisticDto implements Serializable {

    private BigDecimal population;

    private BigDecimal urban;

    private BigDecimal gdp;
}
