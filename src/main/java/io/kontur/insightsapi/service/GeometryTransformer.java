package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class GeometryTransformer {

    private final ObjectMapper objectMapper;

    private static final String ERROR_MESSAGE = "No geometry provided";
    private final Logger logger = LoggerFactory.getLogger(GeometryTransformer.class);

    public String transform(String geoJsonString, Boolean argNullCheck) throws JsonProcessingException {
        GeoJSON geoJSON;
        try {
            geoJSON = GeoJSONFactory.create(geoJsonString);
        } catch (Exception e) {
            String error = String.format("Provided geojson is not valid. Geojson: %s", geoJsonString);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
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
            logger.error(ERROR_MESSAGE);
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
            logger.error(ERROR_MESSAGE);
            throw new NullPointerException(ERROR_MESSAGE);
        }
    }
}