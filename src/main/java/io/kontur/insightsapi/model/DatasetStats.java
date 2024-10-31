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

    private Double minValue;  // min value of all resolution hexes

    private Double maxValue;  // max value of all resolution hexes

    private Double mean;  // mean value of all resolution hexes

    private Double stddev;  // stddev value of all resolution hexes

}
