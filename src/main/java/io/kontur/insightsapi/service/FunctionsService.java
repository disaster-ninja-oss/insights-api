package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.Functions;

public class FunctionsService {

    public Functions calculateFunctionsResult() {

    }

    private String createFunctionBody(String fieldName, FunctionArgs functionArgs) {
        return switch (functionArgs.getName()) {
            case "sumX" -> "sum(" + functionArgs.getX() + ") as "+fieldName;
            case "sumXWhereNoY" -> "sum(" + functionArgs.getX() + "*(1 - sign(" + functionArgs.getY() + ")))";
            case "percentageXWhereNoY" -> "sum(" + functionArgs.getX() + "*(1 - sign(" + functionArgs.getY() + ")))/sum(" + functionArgs.getX() + ")*100";
            default -> null;
        };
    }
}
