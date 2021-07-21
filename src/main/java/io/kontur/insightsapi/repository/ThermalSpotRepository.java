package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.service.Helper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ThermalSpotRepository {

    private static final Map<String, String> queryMap = Map.of(
            "industrialArea", "sum(industrial_area) as industrialArea ",
            "wildfires", "max(wildfires) as wildfires ",
            "volcanoesCount", "sum(volcanos_count)  as volcanoesCount ",
            "forestAreaKm2", "sum(forest) as forestAreaKm2"
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Helper helper;

    @Transactional(readOnly = true)
    public ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList) {
        var queryList = helper.transformFieldList(fieldList, queryMap);
        var paramSource = new MapSqlParameterSource("polygon", geojson);
        var query = "with subdivided_polygon as (" +
                "    select ST_Subdivide(" +
                "                   ST_CollectionExtract(" +
                "                           ST_MakeValid(" +
                "                                   ST_Transform(" +
                "                                       ST_WrapX(ST_WrapX(" +
                "                                           ST_GeomFromGeoJSON(:polygon::json),-180, 360), 180, -360), 3857)), " +
                "                    3), 150) as geom) " +
                "select " + StringUtils.join(queryList, ", ") + " from stat_h3 sh3, subdivided_polygon sp " +
                "where ST_Intersects(sh3.geom, sp.geom) and resolution = 8";
        return namedParameterJdbcTemplate.queryForObject(query, paramSource, (rs, rowNum) ->
                ThermalSpotStatistic.builder()
                        .industrialArea(rs.getBigDecimal("industrialArea"))
                        .wildfires(rs.getLong("wildfires"))
                        .volcanoesCount(rs.getLong("volcanoesCount"))
                        .forestAreaKm2(rs.getBigDecimal("forestAreaKm2"))
                        .build());
    }
}
