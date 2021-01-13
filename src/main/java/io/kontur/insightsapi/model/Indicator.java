package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Indicator {

    private String name;

    private String label;

    private List<String> copyrights;
}
