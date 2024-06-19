package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransformationInfo {
    private List<Transformation> transformation;

    public TransformationInfo(List<Transformation> transformation) {
        this.transformation = transformation;
    }
}
