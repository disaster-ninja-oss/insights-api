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
public class Unit implements Serializable {

    @Serial
    private static final long serialVersionUID = 3682411163405462958L;

    private String id;

    private String shortName;

    private String longName;
}
