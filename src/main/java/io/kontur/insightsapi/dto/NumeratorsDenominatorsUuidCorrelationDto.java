package io.kontur.insightsapi.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NumeratorsDenominatorsUuidCorrelationDto extends NumeratorsDenominatorsUuidDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1738849030701823047L;

    private Double metrics;
}
