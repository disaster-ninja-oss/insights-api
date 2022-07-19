package io.kontur.insightsapi.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class Quotient {

    private String name;
    private String label;
    private List<List<String>> direction;
}
