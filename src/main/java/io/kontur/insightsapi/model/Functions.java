package io.kontur.insightsapi.model;

import io.kontur.insightsapi.dto.FunctionArgs;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Functions {

    private List<FunctionResult> functions;

    public List<FunctionResult> getFunctions(List<FunctionArgs> args) {
        return functions;
    }
}
