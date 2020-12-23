package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Overlay {

    private String name;

    private String description;

    private Boolean active;

    private List<Color> colors;

    private Axis x;

    private Axis y;
}
