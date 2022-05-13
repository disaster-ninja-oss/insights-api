package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.cacheable.ThermalSpotStatisticService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ThermalSpotRepository implements ThermalSpotStatisticService {

    @Value("classpath:/sql.queries/thermal_statistic.sql")
    private Resource thermalStatistic;

    private static final Map<String, String> queryMap = Map.of(
            "industrialAreaKm2", "sum(industrial_area) as industrialAreaKm2 ",
            "hotspotDaysPerYearMax", "max(wildfires) as hotspotDaysPerYearMax ",
            "volcanoesCount", "sum(volcanos_count)  as volcanoesCount ",
            "forestAreaKm2", "sum(forest) as forestAreaKm2"
    );

    private final Logger logger = LoggerFactory.getLogger(ThermalSpotRepository.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Helper helper;

    private final QueryFactory queryFactory;

    @Transactional(readOnly = true)
    public ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList) {
        var queryList = helper.transformFieldList(fieldList, queryMap);
        var paramSource = new MapSqlParameterSource("polygon", geojson);
        var query = String.format(queryFactory.getSql(thermalStatistic), StringUtils.join(queryList, ", "));
        try {
            return namedParameterJdbcTemplate.queryForObject(query, paramSource, (rs, rowNum) ->
                    ThermalSpotStatistic.builder()
                            .industrialAreaKm2(rs.getBigDecimal("industrialAreaKm2"))
                            .hotspotDaysPerYearMax(rs.getLong("hotspotDaysPerYearMax"))
                            .volcanoesCount(rs.getLong("volcanoesCount"))
                            .forestAreaKm2(rs.getBigDecimal("forestAreaKm2"))
                            .build());
        } catch (DataAccessResourceFailureException e) {
            String error = String.format(DatabaseUtil.ERROR_TIMEOUT, geojson);
            logger.error(error, e);
            throw new DataAccessResourceFailureException(error, e);
        } catch (EmptyResultDataAccessException e) {
            String error = String.format(DatabaseUtil.ERROR_EMPTY_RESULT, geojson);
            logger.error(error, e);
            throw new EmptyResultDataAccessException(error, 1);
        } catch (Exception e) {
            String error = String.format(DatabaseUtil.ERROR_SQL, geojson);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }
}