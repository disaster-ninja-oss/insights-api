package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Service
@RequiredArgsConstructor
public class GeometryTransformer {

    private final ObjectMapper objectMapper;

    public String transform(String geoJson) throws JsonProcessingException {
        var jsonNode = objectMapper.readTree(geoJson);
        var type = Optional.ofNullable(jsonNode.get("type"))
                .map(JsonNode::textValue)
                .orElse("");
        if ("FeatureCollection".equals(type)) {
            return transformToGeometryCollection(jsonNode);
        }
        return geoJson;
    }

    private String transformToGeometryCollection(JsonNode jsonNode) throws JsonProcessingException {
        var result = JsonNodeFactory.instance.objectNode();
        result.put("type", "GeometryCollection");
        var geometries = stream(jsonNode.get("features").spliterator(), false)
                .map(current -> current.get("geometry"))
                .collect(toList());
        result.putArray("geometries").addAll(geometries);
        return objectMapper.writeValueAsString(result);
    }
}
