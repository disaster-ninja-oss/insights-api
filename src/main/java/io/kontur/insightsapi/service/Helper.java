package io.kontur.insightsapi.service;

import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Helper {

    public List<String> transformFieldList(List<String> fieldList, Map<String, String> queryMap){
        var queryList = new ArrayList<String>();
        queryMap.forEach((key, value) -> {
            if (fieldList.contains(key)) {
                queryList.add(value);
            } else {
                queryList.add(String.format("NULL as %s", key));
            }
        });
        return queryList;
    }

    public String getPolygonFromRequest(DataFetchingEnvironment environment){
        var arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getParent().getArguments().get("polygonStatisticRequest");
        return (String) arguments.get("polygon");
    }
}
