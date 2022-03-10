package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AdvancedAnalyticsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsRepository.class);

    @Transactional(readOnly = true)
    public List<BivariativeAxisDto> getBivariativeAxis() {
        return namedParameterJdbcTemplate.query(QueryFactory.getBivariativeAxis_query(), (rs, rowNum) -> BivariativeAxisDto.builder()
                .numerator(rs.getString(BivariateAxisColumns.numerator.name()))
                .denominator(rs.getString(BivariateAxisColumns.denominator.name()))
                .numeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()))
                .denominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name())).build());
    }

    public String getQueryWithGeom(List<BivariativeAxisDto> argAxisDto) {
        List<String> bivariativeAxisDistincList = argAxisDto.stream().flatMap(dto -> Stream.of(dto.getNumerator(), dto.getDenominator())).distinct().toList();

        return String.format(QueryFactory.getQueryWithGeom_query(), StringUtils.join(bivariativeAxisDistincList, ","));
    }

    public String getUnionQuery(BivariativeAxisDto numDen) {
        return String.format(QueryFactory.getUnionQuery_query(), numDen.getNumerator(), numDen.getDenominator());
    }

    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getWorldData() {
        List<AdvancedAnalytics> returnList = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(QueryFactory.getWorldData_query(), (rs -> {
                AdvancedAnalytics advancedAnalytics = new AdvancedAnalytics();
                advancedAnalytics.setNumerator(rs.getString(BivariateAxisColumns.numerator.name()));
                advancedAnalytics.setDenominator(rs.getString(BivariateAxisColumns.denominator.name()));
                advancedAnalytics.setNumeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()));
                advancedAnalytics.setDenominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name()));
                advancedAnalytics.setAnalytics(createValuesList(rs));
                returnList.add(advancedAnalytics);
            }));
        } catch (Exception e) {
            String error = String.format("Can't get value from result set %s", e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }
        return returnList;
    }

    @Transactional(readOnly = true)
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", argGeometry);

        List<List<AdvancedAnalyticsValues>> result = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(argQuery, paramSource, (rs -> {
                result.add(createValuesList(rs));
            }));
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s. Exception: %s", argGeometry, e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    private List<AdvancedAnalyticsValues> createValuesList(ResultSet rs) {
        //calculation list will be parametric, for now its constant
        List<String> calculationsList = Stream.of(Calculations.values()).map(Calculations::name).toList();
        return calculationsList.stream().map(arg -> new AdvancedAnalyticsValues(arg,
                DatabaseUtil.getNullableDouble(rs, arg + "_value"),
                DatabaseUtil.getNullableDouble(rs, arg + "_quality"))).toList();
    }

    private enum Calculations {
        sum, min, max, mean, stddev, median
    }

    private enum BivariateAxisColumns {
        numerator, denominator, numerator_label, denominator_label
    }
}
