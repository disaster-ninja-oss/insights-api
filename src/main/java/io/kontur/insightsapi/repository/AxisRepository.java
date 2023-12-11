package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.dto.AxisOverridesRequest;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AxisRepository {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorRepository.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final JdbcTemplate jdbcTemplate;

    @Value("${calculations.bivariate.indicators.test.table}")
    private String bivariateIndicatorsMetadataTableName;

    @Value("classpath:/sql.queries/direct_quality_estimation.sql")
    private Resource qualityEstimation;

    @Value("classpath:/sql.queries/axis_stops_estimation.sql")
    private Resource axisStopsEstimation;

    @Value("classpath:/sql.queries/16269_bivariate_axis_analytics.sql")
    private Resource bivariateAxisAnalytics;

    @Value("classpath:/sql.queries/delete_axis.sql")
    private Resource deleteAxis;

    @Value("classpath:/sql.queries/insert_axis.sql")
    private Resource insertAxis;

    @Value("${calculations.bivariate.axis.test.table}")
    private String bivariateAxisV2TableName;

    private final QueryFactory queryFactory;

    public void deleteAxisIfExist(List<BivariateIndicatorDto> indicatorsForAxis) {
        List<String> params = indicatorsForAxis.stream()
                .map(bivariateIndicatorDto -> "'" + bivariateIndicatorDto.getUuid() + "'")
                .toList();
        String paramsAsString = StringUtils.join(params, ", ");

        var query = String.format(queryFactory.getSql(deleteAxis), bivariateAxisV2TableName, paramsAsString,
                paramsAsString);

        try {
            jdbcTemplate.update(query);
        } catch (Exception e) {
            String error = String.format("Exception while deleting axis: %s", paramsAsString);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

    public void insertOverrides(AxisOverridesRequest request)
            throws IllegalArgumentException {
        String numerator = request.getNumerator_uuid(), denominator = request.getDenominator_uuid();
        if (numerator == null || denominator == null)
            throw new IllegalArgumentException("Numerator and denominator UUIDs cannot be null");
        String sql = """
                insert into bivariate_axis_overrides
                (numerator_uuid, denominator_uuid, label, min, max, p25, p75, min_label, p25_label, p75_label, max_label)
                values
                (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (numerator_uuid, denominator_uuid) do update
                set
                    label = excluded.label,
                    min = excluded.min,
                    max = excluded.max,
                    p25 = excluded.p25,
                    p75 = excluded.p75,
                    min_label = excluded.min_label,
                    max_label = excluded.max_label,
                    p25_label = excluded.p25_label,
                    p75_label = excluded.p75_label
        """;
        try {
            jdbcTemplate.update(
                sql,
                numerator,
                denominator,
                request.getLabel(),
                request.getMin(),
                request.getMax(),
                request.getP25(),
                request.getP75(),
                request.getMinLabel(),
                request.getP25Label(),
                request.getP75Label(),
                request.getMaxLabel()
                );
        } catch (DataIntegrityViolationException e) {
            logger.error("Could not update bivariate_axis_overrides due to FK constraint", e);
            // not-null constraint violation is also DataIntegrityViolationException, but we catch it earlier
            throw new IllegalArgumentException(
                    String.format("Could not update bivariate_axis_overrides: no indicator with uuis=%s found",
                                  e.getMessage().contains("fk_ba_overrides_denominator_uuid") ? denominator : numerator));
        } catch (Exception e) {
            logger.error("Could not update bivariate_axis_overrides.", e);
            throw new IllegalArgumentException("Could not update bivariate_axis_overrides.", e);
        }
    }

    public void uploadAxis(List<BivariativeAxisDto> axisForCurrentIndicators) {
        Map<String, Object>[] batchOfInputs = new HashMap[axisForCurrentIndicators.size()];

        int count = 0;
        for (BivariativeAxisDto bivariativeAxisDto : axisForCurrentIndicators) {
            Map<String, Object> map = new HashMap<>();
            map.put("numerator", bivariativeAxisDto.getNumerator());
            map.put("numerator_uuid", bivariativeAxisDto.getNumerator_uuid());
            map.put("denominator", bivariativeAxisDto.getDenominator());
            map.put("denominator_uuid", bivariativeAxisDto.getDenominator_uuid());
            batchOfInputs[count++] = map;
        }

        try {
            namedParameterJdbcTemplate.batchUpdate(String.format(queryFactory.getSql(insertAxis),
                    bivariateAxisV2TableName), batchOfInputs);
        } catch (Exception e) {
            logger.error("Could not insert axis.", e);
            throw new IllegalArgumentException("Could not insert axis.", e);
        }
    }

    public void calculateStopsAndQuality(BivariativeAxisDto bivariativeAxisDto) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("numerator_uuid", bivariativeAxisDto.getNumerator_uuid());
        paramSource.addValue("denominator_uuid", bivariativeAxisDto.getDenominator_uuid());

        String query = String.format(queryFactory.getSql(qualityEstimation), bivariateAxisV2TableName);
        calculateAndUpdate(query, paramSource);

        query = String.format(queryFactory.getSql(axisStopsEstimation), bivariateAxisV2TableName);
        calculateAndUpdate(query, paramSource);

        query = String.format(queryFactory.getSql(bivariateAxisAnalytics), bivariateAxisV2TableName);
        calculateAndUpdate(query, paramSource);
    }

    private void calculateAndUpdate(String query, MapSqlParameterSource paramSource) {
        try {
            namedParameterJdbcTemplate.update(query, paramSource);
        } catch (Exception e) {
            String error = String.format("Could not estimate stops or quality for numerator_uuid = %s, " +
                            "denominator_uuid = %s",
                    paramSource.getValue("numerator_uuid"),
                    paramSource.getValue("denominator_uuid"));
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }
}
