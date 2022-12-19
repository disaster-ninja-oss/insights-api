package io.kontur.insightsapi.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NumeratorsDenominatorsUuidDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 4944284163735367475L;

    private UUID xNumUuid;

    private UUID xDenUuid;

    private UUID yNumUuid;

    private UUID yDenUuid;
}
