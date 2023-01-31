package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AxisRepository {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorRepository.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("classpath:/sql.queries/direct_quality_estimation.sql")
    private Resource qualityEstimation;

    @Value("classpath:/sql.queries/axis_stops_estimation.sql")
    private Resource axisStopsEstimation;

    @Value("classpath:/sql.queries/delete_axis.sql")
    private Resource deleteAxis;

    @Value("classpath:/sql.queries/insert_axis.sql")
    private Resource insertAxis;

    @Value("${database.bivariate.axis.table}")
    private String bivariateAxisTableName;

    private final QueryFactory queryFactory;

    public void deleteAxisIfExist(List<BivariateIndicatorDto> indicatorsForAxis) {
        List<String> params = indicatorsForAxis.stream()
                .map(bivariateIndicatorDto -> "'" + bivariateIndicatorDto.getUuid() + "'")
                .toList();
        String paramsAsString = StringUtils.join(params, ", ");
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("bivariate_axis", bivariateAxisTableName);

        var query = String.format(queryFactory.getSql(deleteAxis), paramsAsString, paramsAsString);

        try {
            namedParameterJdbcTemplate.update(query, paramSource);
        } catch (Exception e) {
            String error = String.format("Exception while deleting axis: %s", paramsAsString);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

    public void uploadAxis(List<BivariativeAxisDto> axisForCurrentIndicators) {
        Map<String, Object>[] batchOfInputs = new HashMap[axisForCurrentIndicators.size()];

        int count = 0;
        for (BivariativeAxisDto bivariativeAxisDto : axisForCurrentIndicators) {
            Map<String, Object> map = new HashMap<>();
            map.put("bivariate_axis", bivariateAxisTableName);
            map.put("numerator", bivariativeAxisDto.getNumerator());
            map.put("numerator_uuid", bivariativeAxisDto.getNumerator_uuid());
            map.put("denominator", bivariativeAxisDto.getDenominator());
            map.put("denominator_uuid", bivariativeAxisDto.getDenominator_uuid());
            batchOfInputs[count++] = map;
        }

        try {
            namedParameterJdbcTemplate.batchUpdate(queryFactory.getSql(insertAxis), batchOfInputs);
        } catch (Exception e) {
            logger.error("Could not insert axis.", e);
            throw new IllegalArgumentException("Could not insert axis.", e);
        }
    }

    public void calculateStopsAndQuality(BivariativeAxisDto bivariativeAxisDto) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("numerator_uuid", "'" + bivariativeAxisDto.getNumerator_uuid() + "'");
        paramSource.addValue("denominator_uuid", "'" + bivariativeAxisDto.getDenominator_uuid() + "'");
        paramSource.addValue("bivariate_axis", bivariateAxisTableName);

        String query = queryFactory.getSql(qualityEstimation);

        calculateAndUpdate(query, paramSource);

        query = queryFactory.getSql(axisStopsEstimation);

        calculateAndUpdate(query, paramSource);
    }

    private void calculateAndUpdate(String query, MapSqlParameterSource paramSource) {
        try {
            namedParameterJdbcTemplate.update(query, paramSource);
        } catch (Exception e) {
            String error = String.format("Could not estimate stops or quality for numerator_uuid = %s, denominator_uuid = %s",
                    paramSource.getValue("numerator_uuid"), paramSource.getValue("denominator_uuid"));
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
    }

}
