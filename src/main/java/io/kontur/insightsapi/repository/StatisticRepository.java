package io.kontur.insightsapi.repository;

import com.google.common.collect.Lists;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsUuidCorrelationDto;
import io.kontur.insightsapi.mapper.*;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonMetrics;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.service.cacheable.CorrelationRateService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class StatisticRepository implements CorrelationRateService {

    @Value("classpath:/sql.queries/statistic_all.sql")
    private Resource statisticAll;

    @Value("classpath:/sql.queries/bivariate_statistic.sql")
    private Resource bivariateStatistic;

    @Value("classpath:/sql.queries/bivariate_statistic_v2.sql")
    private Resource bivariateStatisticV2;

    @Value("classpath:/sql.queries/axis_statistic.sql")
    private Resource axisStatistic;

    @Value("classpath:/sql.queries/statistic_correlation.sql")
    private Resource statisticCorrelation;

    @Value("classpath:/sql.queries/statistic_correlation_v2.sql")
    private Resource statisticCorrelationV2;

    @Value("classpath:/sql.queries/statistic_correlation_numdenom.sql")
    private Resource statisticCorrelationNumdenom;

    @Value("classpath:/sql.queries/statistic_correlation_numdenom_with_uuid.sql")
    private Resource statisticCorrelationNumdenomWithUuid;

    @Value("classpath:/sql.queries/statistic_correlation_intersect.sql")
    private Resource statisticCorrelationIntersect;

    @Value("classpath:/sql.queries/statistic_correlation_intersect_all_indicators.sql")
    private Resource statisticCorrelationIntersectAll;

    @Value("classpath:/sql.queries/statistic_correlation_emptylayer_intersect.sql")
    private Resource statisticCorrelationEmptylayerIntersect;

    @Value("${calculations.bivariate.indicators.test.table}")
    private String bivariateIndicatorsMetadataTableName;

    @Value("${calculations.bivariate.indicators.table}")
    private String bivariateIndicatorsTableName;

    @Value("${calculations.bivariate.axis.test.table}")
    private String bivariateAxisV2TableName;

    @Value("${calculations.bivariate.axis.table}")
    private String bivariateAxisTableName;

    @Value("${calculations.bivariate.correlations.test.table}")
    private String bivariateAxisCorrelationV2TableName;

    @Value("${calculations.bivariate.correlations.table}")
    private String bivariateAxisCorrelationTableName;

    @Value("${calculations.useStatSeparateTables:false}")
    private Boolean useStatSeparateTables;

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final StatisticRowMapper statisticRowMapper;

    private final BivariateStatisticRowMapper bivariateStatisticRowMapper;

    private final AxisRowMapper axisRowMapper;

    private final PolygonMetricsRowMapper polygonMetricsRowMapper;

    private final CorrelationRateRowMapper correlationRateRowMapper;

    private final QueryFactory queryFactory;

    private final Logger logger = LoggerFactory.getLogger(StatisticRepository.class);

    @Transactional(readOnly = true)
    public Statistic getAllStatistic() {
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName : bivariateIndicatorsTableName;
        String bivariateAxisTable = useStatSeparateTables ? bivariateAxisV2TableName : bivariateAxisTableName;
        String bivariateAxisCorrelationTable = useStatSeparateTables
                ? bivariateAxisCorrelationV2TableName : bivariateAxisCorrelationTableName;
        String filterReady = useStatSeparateTables ? """
            and bix1.state = 'READY'
            and bix2.state = 'READY'
            and biy1.state = 'READY'
            and biy2.state = 'READY'
        """ : "";

        return jdbcTemplate.queryForObject(String.format(queryFactory.getSql(statisticAll), bivariateIndicatorsTable,
                        bivariateAxisCorrelationTable, bivariateIndicatorsTable, bivariateIndicatorsTable,
                        bivariateAxisTable, bivariateAxisTable, bivariateAxisTable, bivariateIndicatorsTable,
                        bivariateIndicatorsTable, bivariateIndicatorsTable, bivariateIndicatorsTable,
                        filterReady, bivariateAxisTable, bivariateAxisTable),
                statisticRowMapper);
    }

    @Transactional(readOnly = true)
    public BivariateStatistic getBivariateStatistic() {
        if (useStatSeparateTables) {
            return jdbcTemplate.queryForObject(queryFactory.getSql(bivariateStatisticV2), bivariateStatisticRowMapper);
        }
        String bivariateIndicatorsTable = bivariateIndicatorsTableName;
        String bivariateAxisTable = bivariateAxisTableName;

        return jdbcTemplate.queryForObject(String.format(queryFactory.getSql(bivariateStatistic),
                        bivariateIndicatorsTable, bivariateAxisTable, bivariateAxisTable, bivariateAxisTable,
                        bivariateIndicatorsTable, bivariateIndicatorsTable, bivariateIndicatorsTable, bivariateIndicatorsTable,
                        bivariateAxisTable, bivariateAxisTable, bivariateIndicatorsTable, bivariateIndicatorsTable,
                        bivariateIndicatorsTable, bivariateIndicatorsTable),
                bivariateStatisticRowMapper);
    }

    @Transactional(readOnly = true)
    public List<Axis> getAxisStatistic() {
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName : bivariateIndicatorsTableName;
        String bivariateAxisTable = useStatSeparateTables ? bivariateAxisV2TableName : bivariateAxisTableName;
        String defaultTransform = useStatSeparateTables ? "default_transform" : "null";
        String where = useStatSeparateTables ? """
                numerator_uuid = bi1.internal_id
            and denominator_uuid = bi2.internal_id
            and bi1.state='READY' and bi2.state='READY'
        """ : """
            numerator = bi1.param_id and denominator = bi2.param_id
        """;

        return jdbcTemplate.query(String.format(queryFactory.getSql(axisStatistic),
                        defaultTransform, bivariateAxisTable, bivariateIndicatorsTable, bivariateIndicatorsTable, where),
                axisRowMapper);
    }

    @Transactional(readOnly = true)
    public List<PolygonMetrics> getAllCorrelationRateStatistics() {
        if (useStatSeparateTables) {
            return jdbcTemplate.query(queryFactory.getSql(statisticCorrelationV2), polygonMetricsRowMapper);
        }
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName : bivariateIndicatorsTableName;
        String bivariateAxisCorrelationTable = useStatSeparateTables
                ? bivariateAxisCorrelationV2TableName : bivariateAxisCorrelationTableName;
        return jdbcTemplate.query(String.format(queryFactory.getSql(statisticCorrelation),
                        bivariateAxisCorrelationTable, bivariateIndicatorsTable, bivariateIndicatorsTable),
                polygonMetricsRowMapper);
    }

    @Transactional(readOnly = true)
    public List<PolygonMetrics> getAllCovarianceRateStatistics() {
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName : bivariateIndicatorsTableName;
        String bivariateAxisCorrelationTable = useStatSeparateTables
                ? bivariateAxisCorrelationV2TableName : bivariateAxisCorrelationTableName;
        //TODO: the same as correlations, just for example. Will be changed in future
        return jdbcTemplate.query(String.format(queryFactory.getSql(statisticCorrelation),
                        bivariateAxisCorrelationTable, bivariateIndicatorsTable, bivariateIndicatorsTable),
                polygonMetricsRowMapper);
    }

    @Transactional(readOnly = true)
    public List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsWithUuidForCorrelation() {
        return jdbcTemplate.query(String.format(queryFactory.getSql(statisticCorrelationNumdenomWithUuid),
                        bivariateAxisV2TableName, bivariateIndicatorsMetadataTableName, bivariateIndicatorsMetadataTableName,
                        bivariateAxisV2TableName, bivariateIndicatorsMetadataTableName, bivariateIndicatorsMetadataTableName),
                (rs, rowNum) -> NumeratorsDenominatorsDto.builder()
                        .xNumerator(rs.getString("x_num"))
                        .xDenominator(rs.getString("x_den"))
                        .xLabel(rs.getString("x_param_label"))
                        .xNumUuid(rs.getObject("x_num_internal_id", UUID.class))
                        .xDenUuid(rs.getObject("x_den_internal_id", UUID.class))
                        .yNumerator(rs.getString("y_num"))
                        .yDenominator(rs.getString("y_den"))
                        .yLabel(rs.getString("y_param_label"))
                        .yNumUuid(rs.getObject("y_num_internal_id", UUID.class))
                        .yDenUuid(rs.getObject("y_den_internal_id", UUID.class))
                        .quality(rs.getDouble("quality")).build());
    }

    @Transactional(readOnly = true)
    public List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation() {
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName : bivariateIndicatorsTableName;
        String bivariateAxisTable = useStatSeparateTables ? bivariateAxisV2TableName : bivariateAxisTableName;

        return jdbcTemplate.query(String.format(queryFactory.getSql(statisticCorrelationNumdenom),
                        bivariateAxisTable, bivariateIndicatorsTable, bivariateIndicatorsTable, bivariateAxisTable,
                        bivariateIndicatorsTable), (rs, rowNum) ->
                NumeratorsDenominatorsDto.builder()
                        .xNumerator(rs.getString("x_num"))
                        .xDenominator(rs.getString("x_den"))
                        .xLabel(rs.getString("x_param_label"))
                        .yNumerator(rs.getString("y_num"))
                        .yDenominator(rs.getString("y_den"))
                        .yLabel(rs.getString("y_param_label"))
                        .quality(rs.getDouble("quality")).build());
    }

    @Transactional(readOnly = true)
    public List<NumeratorsDenominatorsUuidCorrelationDto> getPolygonCorrelationRateStatistics(String polygon) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", polygon);
        var query = String.format(queryFactory.getSql(statisticCorrelationIntersectAll), bivariateIndicatorsMetadataTableName);
        try {
            return namedParameterJdbcTemplate.query(query, paramSource, (rs, rowNum) ->
                    NumeratorsDenominatorsUuidCorrelationDto.builder()
                            .xNumUuid(rs.getObject("xNumUuid", UUID.class))
                            .xDenUuid(rs.getObject("xDenUuid", UUID.class))
                            .yNumUuid(rs.getObject("yNumUuid", UUID.class))
                            .yDenUuid(rs.getObject("yDenUuid", UUID.class))
                            .metrics(rs.getDouble("metrics"))
                            .build());
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s", polygon);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

    @Transactional(readOnly = true)
    public List<Double> getPolygonCorrelationRateStatisticsBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", polygon);
        var requests = dtoList.stream()
                .map(dto -> createCorrelationQueryString(dto.getXNumerator(), dto.getXDenominator(),
                        dto.getYNumerator(), dto.getYDenominator())).toList();
        var distinctFieldsRequests = dtoList.stream()
                .flatMap(dto -> Stream.of(dto.getXNumerator(), dto.getYNumerator(), dto.getXDenominator(), dto.getYDenominator()))
                .distinct()
                .toList();
        var query = String.format(queryFactory.getSql(statisticCorrelationIntersect), StringUtils.join(distinctFieldsRequests, ","), StringUtils.join(requests, ","));
        //it is important to disable jit in same stream with main request
        jitDisable();
        try {
            return namedParameterJdbcTemplate.queryForObject(query, paramSource, correlationRateRowMapper);
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s", polygon);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

    @Transactional(readOnly = true)
    public List<Double> getPolygonCovarianceRateStatisticsBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        //TODO: the same as correlations, just for example. Will be changed in future
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", polygon);
        var requests = dtoList.stream()
                .map(dto -> createCovarianceQueryString(dto.getXNumerator(), dto.getXDenominator(),
                        dto.getYNumerator(), dto.getYDenominator())).toList();
        var distinctFieldsRequests = dtoList.stream()
                .flatMap(dto -> Stream.of(dto.getXNumerator(), dto.getYNumerator(), dto.getXDenominator(), dto.getYDenominator()))
                .distinct()
                .toList();
        var query = String.format(queryFactory.getSql(statisticCorrelationIntersect), StringUtils.join(distinctFieldsRequests, ","), StringUtils.join(requests, ","));
        //it is important to disable jit in same stream with main request
        jitDisable();
        try {
            return namedParameterJdbcTemplate.queryForObject(query, paramSource, correlationRateRowMapper);
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s", polygon);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(String polygon, List<NumeratorsDenominatorsDto> dtoList) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", polygon);
        var distinctFieldsRequests = dtoList.stream()
                .flatMap(dto -> Stream.of(dto.getXNumerator(), dto.getYNumerator()))
                .distinct()
                .collect(Collectors.toList());
        List<String> requests = Lists.newArrayList();
        for (String field : distinctFieldsRequests) {
            requests.add("max(" + field + ")!=min(" + field + ") as result" + field);
        }
        var query = String.format(queryFactory.getSql(statisticCorrelationEmptylayerIntersect), StringUtils.join(requests, ","), StringUtils.join(distinctFieldsRequests, ","));
        //it is important to disable jit in same stream with main request
        jitDisable();
        Map<String, Boolean> result = new HashMap<>();
        try {
            namedParameterJdbcTemplate.query(query, paramSource, (rs -> {
                result.putAll(createResultMapForNotEmptyLayers(distinctFieldsRequests, rs));
            }));
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s. Exception: %s", polygon, e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    private Map<String, Boolean> createResultMapForNotEmptyLayers(List<String> numerators, ResultSet rs) {
        Map<String, Boolean> result = new HashMap<>();
        numerators.forEach(numerator -> {
            try {
                result.put(numerator, rs.getBoolean("result" + numerator));
            } catch (SQLException e) {
                logger.error("Can't get boolean value from result set", e);
            }
        });
        return result;
    }

    private String createCorrelationQueryString(String xNum, String xDen, String yNum, String yDen) {
        return "corr(" + xNum + " / " + xDen + ", " + yNum + " / " + yDen + ") filter (where " + xDen + " != 0 and " + yDen + " != 0)";
    }

    private String createCovarianceQueryString(String xNum, String xDen, String yNum, String yDen) {
        return "covar_samp(" + xNum + " / " + xDen + ", " + yNum + " / " + yDen + ") filter (where " + xDen + " != 0 and " + yDen + " != 0)";
    }

    public void jitDisable() {
        jdbcTemplate.execute("set local jit = off");
    }
}
