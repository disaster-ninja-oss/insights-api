package io.kontur.insightsapi.repository;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final QueryFactory queryFactory;

    @Value("classpath:/sql.queries/get_tile_mvt.sql")
    private Resource getTileMvtResource;

    @Value("classpath:/sql.queries/get_tile_mvt_indicators_list_v2.sql")
    private Resource getTileMvtIndicatorsListResourceV2;

    @Value("${calculations.bivariate.indicators.table}")
    private String bivariateIndicatorsTableName;

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, List<String> bivariateIndicators) {
        var bivariateIndicatorsForQuery = Lists.newArrayList();
        for (String current : bivariateIndicators) {
            bivariateIndicatorsForQuery.add("coalesce(" + current + ", 0) as " + current);
        }
        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        var query = String.format(queryFactory.getSql(getTileMvtResource), StringUtils.join(bivariateIndicatorsForQuery, ", "));
        return namedParameterJdbcTemplate.queryForObject(query, paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
    }

    public byte[] getBivariateTileMvtIndicatorsListV2(Integer z, Integer x, Integer y, List<String> bivariateIndicators){
        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        paramSource.addValue("ind0", bivariateIndicators.get(0));
        paramSource.addValue("ind1", bivariateIndicators.get(1));
        paramSource.addValue("ind2", bivariateIndicators.get(2));
        paramSource.addValue("ind3", bivariateIndicators.get(3));
        var query = String.format(queryFactory.getSql(getTileMvtIndicatorsListResourceV2), bivariateIndicatorsTableName);
        return namedParameterJdbcTemplate.queryForObject(query, paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
    }

    public List<String> getAllBivariateIndicators() {
        var query = "select param_id from bivariate_indicators";
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("param_id"));
    }

    public List<String> getGeneralBivariateIndicators() {
        Set<String> result = new HashSet<>();
        var query = "select x_numerator, x_denominator, y_numerator, y_denominator from bivariate_overlays";
        jdbcTemplate.query(query, (rs, rowNum) ->
                result.addAll(List.of(rs.getString("x_numerator"),
                        rs.getString("x_denominator"),
                        rs.getString("y_numerator"),
                        rs.getString("y_denominator"))));
        return result.stream().toList();
    }
}
