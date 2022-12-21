package io.kontur.insightsapi.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class HelperRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Transactional(readOnly = true)
    public String transformGeometryToWkt(String geometry) {
        var paramSource = new MapSqlParameterSource("geometry", geometry);
        var query = "select ST_AsText(map_to_geometry_obj(:geometry))";
        var geometryString = namedParameterJdbcTemplate
                .queryForObject(query, paramSource, String.class);
        return "SRID=3857;" + geometryString;
    }
}
