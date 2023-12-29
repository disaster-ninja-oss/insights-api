package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final QueryFactory queryFactory;

    @Value("classpath:/sql.queries/get_tile_mvt_generate_on_the_fly.sql")
    private Resource getTileMvtGenerateOnTheFly;

    public byte[] getBivariateTileMvt(Integer resolution, Integer z, Integer x, Integer y,
                                      List<BivariateIndicatorDto> indicators) {

        String query = generateSqlQuery(indicators);

        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        paramSource.addValue("resolution", resolution);

        return namedParameterJdbcTemplate.queryForObject(query, paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
    }

    private String generateSqlQuery(List<BivariateIndicatorDto> indicators) {
        List<String> outerFilter = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        for (BivariateIndicatorDto indicator : indicators) {
            outerFilter.add(String.format("'%s'", indicator.getInternalId()));
            columns.add(String.format("coalesce(avg(indicator_value) filter (where indicator_id = '%s'), 0) as %s",
                    indicator.getInternalId(), indicator.getId()));
        }

        return String.format(queryFactory.getSql(getTileMvtGenerateOnTheFly),
                StringUtils.join(outerFilter, ", "),
                StringUtils.join(columns, ", "));
    }

    public Map<Integer, Integer> initZoomToH3Resolutions(Integer tileSize, Integer hexEdgePixels,
                                                         Integer maxH3Resolutions, Integer minH3Resolutions,
                                                         Integer maxZoom, Integer minZoom) {
        Map<Integer, Integer> result = new HashMap<>();
        var paramSource = new MapSqlParameterSource("tile_size", tileSize);
        paramSource.addValue("hex_edge_pixels", hexEdgePixels);
        paramSource.addValue("max_h3_resolution", maxH3Resolutions);
        paramSource.addValue("min_h3_resolution", minH3Resolutions);
        paramSource.addValue("max_zoom", maxZoom);
        paramSource.addValue("min_zoom", minZoom);
        var query = "select i as zoom, tile_zoom_level_to_h3_resolution(i, :max_h3_resolution, :min_h3_resolution, " +
                ":hex_edge_pixels, :tile_size) as resolution from generate_series(:min_zoom, :max_zoom) i;";
        List<Map<String, Object>> listRes = namedParameterJdbcTemplate.queryForList(query, paramSource);
        for (Map<String, Object> item : listRes) {
            try {
                result.put(Integer.valueOf(item.get("zoom").toString()),
                        Integer.valueOf(item.get("resolution").toString()));
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}
