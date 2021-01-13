package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Color {

    private String fallback;

    private List<Combination> combinations;
}
