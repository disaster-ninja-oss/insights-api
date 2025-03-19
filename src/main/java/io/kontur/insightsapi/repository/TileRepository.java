package io.kontur.insightsapi.repository;

import com.google.common.collect.Lists;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final QueryFactory queryFactory;

    @Value("classpath:/sql.queries/get_tile_mvt_indicators_list_v2.sql")
    private Resource getTileMvtIndicatorsListResourceV2;

    @Value("classpath:/sql.queries/get_tile_mvt_generate_on_the_fly.sql")
    private Resource getTileMvtGenerateOnTheFly;

    @Value("classpath:/sql.queries/get_tile_mvt_generate_high_res.sql")
    private Resource getTileMvtGenerateHighRes;

    @Value("${calculations.tiles.tile-size}")
    private Integer tileSize;

    @Value("${calculations.tiles.hex-edge-pixels}")
    private Integer hexEdgePixels;

    @Value("${calculations.tiles.max-h3-resolution}")
    private Integer maxH3Resolutions;

    @Value("${calculations.tiles.min-h3-resolution}")
    private Integer minH3Resolutions;

    @Value("${calculations.tiles.max-zoom}")
    private Integer maxZoom;

    @Value("${calculations.tiles.min-zoom}")
    private Integer minZoom;

    private final IndicatorRepository indicatorRepository;

    public byte[] getBivariateTileMvt(Integer resolution, Integer z, Integer x, Integer y,
                                      List<String> bivariateIndicators) {

        String query = generateSqlQuery(bivariateIndicators, resolution);

        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        paramSource.addValue("resolution", resolution);

        jdbcTemplate.execute("set max_parallel_workers_per_gather = 0");
        byte[] tile = namedParameterJdbcTemplate.queryForObject(query, paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
        jdbcTemplate.execute("reset max_parallel_workers_per_gather");
        return tile;
    }

    public byte[] getBivariateTileMvtIndicatorsListV2(Integer resolution, Integer z, Integer x, Integer y,
                                                      List<String> bivariateIndicators) {
        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        paramSource.addValue("resolution", resolution);
        paramSource.addValue("ind0", bivariateIndicators.get(0));
        paramSource.addValue("ind1", bivariateIndicators.get(1));
        paramSource.addValue("ind2", bivariateIndicators.get(2));
        paramSource.addValue("ind3", bivariateIndicators.get(3));
        return namedParameterJdbcTemplate.queryForObject(queryFactory.getSql(getTileMvtIndicatorsListResourceV2), paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
    }

    public List<String> getAllBivariateIndicators(Boolean publicOnly, List<String> indicators) {
        if (indicators != null) {
            return indicators;
        }
        var query = "select param_id from bivariate_indicators_metadata where state = 'READY'";
        if (publicOnly) {
            query += " and is_public";
        }
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("param_id"));
    }

    private String generateSqlQuery(List<String> bivariateIndicators, Integer resolution) {
        List<BivariateIndicatorDto> bivariateIndicatorDtos =
                indicatorRepository.getSelectedBivariateIndicators(bivariateIndicators);

        List<String> columns = Lists.newArrayList();
        List<String> uuids = Lists.newArrayList();

        for (BivariateIndicatorDto indicator : bivariateIndicatorDtos) {
            if (indicator.getId().equals("one")) {
                columns.add("1.0::float as \"one\"");
            } else if (indicator.getId().equals("area_km2")) {
                columns.add("ST_Area(h3_cell_to_boundary_geography(h3)) / 1000000.0 as \"area_km2\"");
            } else {
                uuids.add("'" + indicator.getInternalId() + "'");
                columns.add(String.format("coalesce(avg(indicator_value) filter (where indicator_uuid = '%s'), 0) as \"%s\"",
                        indicator.getInternalId(), indicator.getId()));
            }
        }

        return resolution > 8 ?
            String.format(queryFactory.getSql(getTileMvtGenerateHighRes), StringUtils.join(uuids, ", "), StringUtils.join(uuids, ", "), StringUtils.join(columns, ", ")) :
            String.format(queryFactory.getSql(getTileMvtGenerateOnTheFly), StringUtils.join(uuids, ", "), StringUtils.join(columns, ", "));

    }

    public Map<Integer, Integer> initZoomToH3Resolutions() {
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
