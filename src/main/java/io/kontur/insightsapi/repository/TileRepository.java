package io.kontur.insightsapi.repository;

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

        // without this setting query planner may want to use parallel seq scan on stat_h3_geom
        jdbcTemplate.execute("set max_parallel_workers_per_gather = 0");
        // with outdated stats tiles tend to be generated using hash join, which is slow and large
        jdbcTemplate.execute("set enable_hashjoin = 'off'");
        // seq scan appears cheaper for small partitions, but becomes too slow when run in many loops
        jdbcTemplate.execute("set enable_seqscan = 'off'");
        // gateway timeout is 60s. tile generation may unexpectedly hang (#21156), we stop the query after a minute to reduce the overall CPU load
        jdbcTemplate.execute("set statement_timeout = '61s'");
        byte[] tile = namedParameterJdbcTemplate.queryForObject(query, paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
        jdbcTemplate.execute("reset max_parallel_workers_per_gather");
        jdbcTemplate.execute("reset enable_hashjoin");
        jdbcTemplate.execute("reset enable_seqscan");
        jdbcTemplate.execute("reset statement_timeout");
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

        List<String> columns = DatabaseUtil.getColumns(bivariateIndicatorDtos);
        List<String> uuids = DatabaseUtil.getUUIDs(bivariateIndicatorDtos);

        return resolution > 8 ?
            String.format(queryFactory.getSql(getTileMvtGenerateHighRes), StringUtils.join(uuids, ", "), StringUtils.join(uuids, ", "), StringUtils.join(columns, ", ")) :
            String.format(
                    queryFactory.getSql(getTileMvtGenerateOnTheFly),
                    DatabaseUtil.buildCTE(
                        resolution.toString(),
                        bivariateIndicatorDtos, """
                            ,ST_AsMVTGeom(geom, ST_TileEnvelope(:z, :x, :y), 8192, 64, true) as geom
                            ,h3index_to_bigint(h3) as h3ind""",
                        true));

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
