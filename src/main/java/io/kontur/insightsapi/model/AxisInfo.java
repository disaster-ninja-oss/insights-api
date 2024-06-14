package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AxisInfo {
    private List<Axis> axis;

    public AxisInfo(List<Axis> axis) {
        this.axis = axis;
    }
}
