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

    private List<List<String>> direction;

    private String description;

    private String coverage;

    private String updateFrequency;

    private Unit unit;

    private String emoji;

    @SuppressWarnings("unused")
    public String getUpdate_frequency() {
        return updateFrequency;
    }

    @SuppressWarnings("unused")
    public void setUpdate_frequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }
}
