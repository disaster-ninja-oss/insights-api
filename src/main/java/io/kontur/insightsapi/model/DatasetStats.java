package io.kontur.insightsapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1722430120010101010L;

    private Double minValue;

    private Double maxValue;

    private Double mean;

    private Double stddev;

}
