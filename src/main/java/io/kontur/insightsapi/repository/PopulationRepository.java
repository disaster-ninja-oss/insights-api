package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.CalculatePopulationDto;
import io.kontur.insightsapi.dto.HumanitarianImpactDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wololo.geojson.GeoJSONFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class PopulationRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Transactional(readOnly = true)
    public Map<String, CalculatePopulationDto> getPopulationAndGdp(String geometry) {
        var paramSource = new MapSqlParameterSource("geometry", geometry);
        var query = "select type, population, urban, gdp" +
                "        from calculate_population_and_gdp_for_wkt(:geometry)";

        return Map.of("population", Objects.requireNonNull(namedParameterJdbcTemplate.queryForObject(query, paramSource, (rs, rowNum) ->
                CalculatePopulationDto.builder()
                        .population(rs.getBigDecimal("population"))
                        .gdp(rs.getBigDecimal("gdp"))
                        .type(rs.getString("type"))
                        .urban(rs.getBigDecimal("urban")).build())));
    }

    @Transactional(readOnly = true)
    public BigDecimal getArea(String geometry) {
        var paramSource = new MapSqlParameterSource("geometry", geometry);
        var query = "select ST_Area(ST_GeomFromText(:geometry))";
        return namedParameterJdbcTemplate.queryForObject(query, paramSource, BigDecimal.class);
    }

    @Transactional(readOnly = true)
    public List<HumanitarianImpactDto> calculateHumanitarianImpact(String wkt) {
        var paramSource = new MapSqlParameterSource("wkt", wkt);
        var query = "        with resolution as (" +
                "            select calculate_area_resolution(ST_SetSRID(:wkt::geometry, 4326)) as resolution" +
                "        )," +
                "            subdivided_input as (" +
                "                select ST_Subdivide(" +
                "                        ST_CollectionExtract(" +
                "                                ST_MakeValid(ST_Transform(" +
                "                                        ST_WrapX(ST_WrapX(" +
                "                                                ST_SetSRID(ST_GeomFromEWKT(:wkt), 4326)," +
                "                                                180, -360), -180, 360)," +
                "                                        3857))," +
                "                                3), 150) as geom" +
                "            )," +
                "            stat_in_area as (select s.*, sum(population) over (order by population desc) as sum_pop" +
                "                             from (select distinct population, s.geom, area_km2, s.h3 as h3" +
                "                                   from stat_h3 s," +
                "                                       subdivided_input i," +
                "                                       resolution r" +
                "                                   where zoom = r.resolution" +
                "                                     and population > 0" +
                "                                     and ST_Intersects(" +
                "                                           s.geom," +
                "                                           i.geom" +
                "                                       )" +
                "                             ) s)," +
                "            total as (select sum(population) as population, round(sum(area_km2)::numeric, 2) as area from stat_in_area)" +
                "        select sum(s.population)                                             as population," +
                "            '68-100'                                                         as percentage," +
                "            'Kontur Settled Periphery'                                       as name," +
                "            round(sum(area_km2)::numeric, 2)                                 as areaKm2," +
                "            ST_AsGeoJSON(ST_Transform(ST_Buffer(St_Collect(geom), 0), 4326)) as geometry," +
                "            t.population                                                     as totalPopulation," +
                "            t.area                                                           as totalAreaKm2" +
                "        from stat_in_area s," +
                "            total t" +
                "        where sum_pop > t.population * 0.68" +
                "        group by t.population, t.area" +
                "        union all" +
                "        select sum(s.population)                                             as population," +
                "            '0-68'                                                           as percentage," +
                "            'Kontur Urban Core'                                              as name," +
                "            round(sum(area_km2)::numeric, 2)                                 as areaKm2," +
                "            ST_AsGeoJSON(ST_Transform(ST_Buffer(St_Collect(geom), 0), 4326)) as geometry," +
                "            t.population                                                     as totalPopulation," +
                "            t.area                                                           as totalAreaKm2" +
                "        from stat_in_area s," +
                "            total t" +
                "        where sum_pop <= t.population * 0.68" +
                "        group by t.population, t.area";
        return namedParameterJdbcTemplate.query(query, paramSource, (rs, rowNum) ->
                HumanitarianImpactDto.builder()
                        .areaKm2(rs.getBigDecimal("areaKm2"))
                        .population(rs.getBigDecimal("population"))
                        .totalPopulation(rs.getBigDecimal("totalPopulation"))
                        .geometry(GeoJSONFactory.create(rs.getString("geometry")))
                        .name(rs.getString("name"))
                        .percentage(rs.getString("percentage"))
                        .totalAreaKm2(rs.getBigDecimal("totalAreaKm2")).build());
    }
}
