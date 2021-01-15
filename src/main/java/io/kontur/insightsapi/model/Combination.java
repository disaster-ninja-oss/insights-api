package io.kontur.insightsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Combination {

    private String color;

    private List<String> corner;

    private String colorComment;

    @SuppressWarnings("unused")
    public String getColor_comment() {
        return this.colorComment;
    }

    @SuppressWarnings("unused")
    public void setColor_comment(String value) {
        this.colorComment = value;
    }
}
