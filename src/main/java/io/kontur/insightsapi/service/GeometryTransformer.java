package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class GeometryTransformer {

    private final ObjectMapper objectMapper;

    private final String ERROR_MESSAGE = "No geometry provided";

    public String transform(String geoJsonString, Boolean argNullCheck) throws JsonProcessingException {
        var geoJSON = GeoJSONFactory.create(geoJsonString);
        var type = geoJSON.getType();
        switch (type) {
            case ("FeatureCollection"):
                return transformToGeometryCollection(geoJSON, argNullCheck);
            case ("Feature"):
                return transformToGeometry(geoJSON, argNullCheck);
            default:
                return geoJsonString;
        }
    }

    private String transformToGeometryCollection(GeoJSON geoJSON, Boolean argNullCheck) throws JsonProcessingException {
        var featureCollection = (FeatureCollection) geoJSON;
        var geometries = Arrays.stream(featureCollection.getFeatures())
                .map(Feature::getGeometry).toList();
        if (geometries.size() > 0) {
            var resultCollection = new GeometryCollection(geometries.toArray(Geometry[]::new));
            return objectMapper.writeValueAsString(resultCollection);
        }
        if (argNullCheck) {
            return null;
        } else {
            throw new NullPointerException(ERROR_MESSAGE);
        }
    }

    private String transformToGeometry(GeoJSON geoJSON, Boolean argNullCheck) throws JsonProcessingException {
        var geometry = ((Feature) geoJSON).getGeometry();
        if (geometry != null) {
            return objectMapper.writeValueAsString(geometry);
        }
        if (argNullCheck) {
            return null;
        } else {
            throw new NullPointerException(ERROR_MESSAGE);
        }
    }
}
