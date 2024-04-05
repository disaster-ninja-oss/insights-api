package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.ThermalSpotStatistic;
import io.kontur.insightsapi.service.cacheable.ThermalSpotStatisticService;
import io.kontur.insightsapi.repository.FunctionsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ThermalSpotRepository implements ThermalSpotStatisticService {

    private static final Map<String, String> funcMap = Map.of(
            "industrialAreaKm2", "sumX",
            "hotspotDaysPerYearMax", "maxX",
            "volcanoesCount", "sumX",
            "forestAreaKm2", "sumX"
    );

    private final Logger logger = LoggerFactory.getLogger(ThermalSpotRepository.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final FunctionsRepository functionsRepository;

    @Transactional(readOnly = true)
    public ThermalSpotStatistic calculateThermalSpotStatistic(String geojson, List<String> fieldList) {
        var paramSource = new MapSqlParameterSource("polygon", geojson);
        System.out.println(geojson);
        List<FunctionArgs> args = fieldList.stream()
            .map(f -> new FunctionArgs(f, funcMap.get(f), f, null))
            .collect(Collectors.toList());
        String query = functionsRepository.getFunctionsQuery(args);
        try {
            return namedParameterJdbcTemplate.queryForObject(query, paramSource, (rs, rowNum) ->
                    ThermalSpotStatistic.builder()
                            .industrialAreaKm2(rs.getBigDecimal("resultindustrialAreaKm2"))
                            .hotspotDaysPerYearMax(rs.getLong("resulthotspotDaysPerYearMax"))
                            .volcanoesCount(rs.getLong("resultvolcanoesCount"))
                            .forestAreaKm2(rs.getBigDecimal("resultforestAreaKm2"))
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
