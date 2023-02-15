package io.kontur.insightsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 820740790464566332L;

    private String id;

    private BigDecimal result;

    private Unit unit;

    private String xLabel;

    private String yLabel;
}
