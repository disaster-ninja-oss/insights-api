package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.FunctionResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FunctionsService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<FunctionResult> calculateFunctionsResult(String geojson, List<FunctionArgs> args) {
        List<String> params = args.stream().map(this::createFunctionBody).toList();
        var paramSource = new MapSqlParameterSource("polygon", geojson);
        var query = String.format("""
                with validated_input as (
                    select ST_MakeValid(ST_Transform(
                            ST_WrapX(ST_WrapX(
                                             ST_Union(ST_MakeValid(
                                                     d.geom
                                                 )),
                                             180, -360), -180, 360),
                            3857)) geom
                    from ST_Dump(ST_CollectionExtract(ST_GeomFromGeoJSON(
                                                              :polygon::jsonb
                                                                     ))) d
                ),
                subdivided_polygons as materialized (
                         select ST_Subdivide(v.geom) geom
                         from validated_input v
                ),
                           stat_area as (
                                         select distinct on (sh3.h3) sh3.h3, sh3.population, sh3.populated_area_km2, sh3.count, 
                sh3.building_count, sh3.highway_length from stat_h3 sh3, subdivided_polygons sp 
                                         where st_dwithin(sh3.geom, sp.geom, 0) and resolution = 8
                                    ) 
                select %s from stat_area st
                """.trim(), StringUtils.join(params, ", "));
        List<FunctionResult> result = new ArrayList<>();
        namedParameterJdbcTemplate.query(query, paramSource, (rs -> {
            result.addAll(args.stream().map(arg-> {
                try {
                    return new FunctionResult(arg.getId(), rs.getBigDecimal("result"+arg.getId()));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }).toList());
        }));
        return result;
    }

    private String createFunctionBody(FunctionArgs functionArgs) {
        return switch (functionArgs.getName()) {
            case "sumX" -> "sum(" + functionArgs.getX() + ") as result"+functionArgs.getId();
            case "sumXWhereNoY" -> "sum(" + functionArgs.getX() + "*(1 - sign(" + functionArgs.getY() + "))) " +
                    "as result"+functionArgs.getId();
            case "percentageXWhereNoY" -> "sum(" + functionArgs.getX() + "*(1 - sign(" + functionArgs.getY() + ")))/sum(" +
                    functionArgs.getX() + ")*100 as result"+functionArgs.getId();
            default -> null;
        };
    }
}
