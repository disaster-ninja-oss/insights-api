package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.AdvancedAnalyticsQualitySortDto;
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
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AdvancedAnalyticsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsRepository.class);

    @Transactional(readOnly = true)
    public List<BivariativeAxisDto> getBivariativeAxis() {
        var query = """
                select ax.numerator, ax.denominator, ind1.param_label numerator_label,
                   ind2.param_label denominator_label
                   from bivariate_axis ax
                   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
                   join bivariate_indicators ind2 on ind2.param_id = ax.denominator
                """.trim();
        return namedParameterJdbcTemplate.query(query, (rs, rowNum) -> BivariativeAxisDto.builder()
                .numerator(rs.getString(BivariateAxisColumns.numerator.name()))
                .denominator(rs.getString(BivariateAxisColumns.denominator.name()))
                .numeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()))
                .denominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name())).build());
    }

    public String getQueryWithGeom(List<BivariativeAxisDto> argAxisDto) {
        List<String> bivariativeAxisDistincList = argAxisDto.stream().flatMap(dto -> Stream.of(dto.getNumerator(), dto.getDenominator())).distinct().toList();

        return String.format("""
                with validated_input as (
                    select calculate_validated_input(:polygon) geom
                ),
                     stat_area as (
                                 select distinct on (h.h3) h.*
                                 from (
                                          select ST_Subdivide(v.geom, 30) geom
                                          from validated_input v
                                      ) p
                                          cross join
                                      lateral (
                                          select h3, %s, resolution
                                          from stat_h3 sh
                                          where ST_Intersects(sh.geom, p.geom)
                                            order by h3
                                          ) h
                     )
                     """.trim(), StringUtils.join(bivariativeAxisDistincList, ","));
    }

    public String getUnionQuery(BivariativeAxisDto numDen) {
        return String.format("""
                 select avg(sum) filter (where r = 8) as sum_value,
                                       case
                                          when (nullif(max(sum),0) / nullif(min(sum), 0)) > 0
                                          then log10(nullif(max(sum), 0) / nullif(min(sum),0))
                                          else log10((nullif(max(sum), 0) - nullif(min(sum),0)) / least(abs(nullif(min(sum),0)), abs(nullif(max(sum),0))))
                                       end as sum_quality,
                                    avg(min) filter (where r = 8) as min_value,
                                       case
                                          when (nullif(max(min),0) / nullif(min(min), 0)) > 0
                                          then log10(nullif(max(min), 0) / nullif(min(min),0))
                                          else log10((nullif(max(min), 0) - nullif(min(min),0)) / least(abs(nullif(min(min),0)), abs(nullif(max(min),0))))
                                       end as min_quality,
                                    avg(max) filter (where r = 8) as max_value,
                                       case
                                          when (nullif(max(max),0) / nullif(min(max), 0)) > 0
                                          then log10(nullif(max(max), 0) / nullif(min(max),0))
                                          else log10((nullif(max(max), 0) - nullif(min(max),0)) / least(abs(nullif(min(max),0)), abs(nullif(max(max),0))))
                                       end as max_quality,
                                    avg(mean) filter (where r = 8) as mean_value,
                                       case
                                          when (nullif(max(mean),0) / nullif(min(mean), 0)) > 0
                                          then log10(nullif(max(mean), 0) / nullif(min(mean),0))
                                          else log10((nullif(max(mean), 0) - nullif(min(mean),0)) / least(abs(nullif(min(mean),0)), abs(nullif(max(mean),0))))
                                       end as mean_quality,
                                    avg(stddev) filter (where r = 8) as stddev_value,
                                       case
                                          when (nullif(max(stddev),0) / nullif(min(stddev), 0)) > 0
                                          then log10(nullif(max(stddev), 0) / nullif(min(stddev),0))
                                          else log10((nullif(max(stddev), 0) - nullif(min(stddev),0)) / least(abs(nullif(min(stddev),0)), abs(nullif(max(stddev),0))))
                                       end as stddev_quality,
                                    avg(median) filter (where r = 8) as median_value,
                                       case
                                          when (nullif(max(median),0) / nullif(min(median), 0)) > 0
                                          then log10(nullif(max(median), 0) / nullif(min(median),0))
                                          else log10((nullif(max(median), 0) - nullif(min(median),0)) / least(abs(nullif(min(median),0)), abs(nullif(max(median),0))))
                                       end as median_quality
                                from ( select r, sum(m), min(m), max(m), avg(m) as mean, stddev(m),
                                       percentile_cont(0.5) within group (order by m) as median
                                    from (select (%s / nullif(%s,0)) as m, resolution as r from stat_area) z
                                    group by r
                                    order by r ) z
                """.trim(), numDen.getNumerator(), numDen.getDenominator());
    }

    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getWorldData() {
        var query = """
                select ax.numerator, ax.denominator, ind1.param_label numerator_label,
                   ind2.param_label denominator_label,
                   ax.sum_value, ax.sum_quality, ax.min_value, ax.min_quality,
                   ax.max_value, ax.max_quality, ax.stddev_value, ax.stddev_quality,
                   ax.median_value, ax.median_quality, ax.mean_value, ax.mean_quality
                   from bivariate_axis ax
                   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
                   join bivariate_indicators ind2 on ind2.param_id = ax.denominator
                """.trim();
        List<BivariativeAxisDto> axisDtos = new ArrayList<>();
        List<List<AdvancedAnalyticsValues>> advancedAnalyticsValues =  new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(query, (rs -> {
                BivariativeAxisDto bivariativeAxisDto = new BivariativeAxisDto();
                bivariativeAxisDto.setNumerator(rs.getString(BivariateAxisColumns.numerator.name()));
                bivariativeAxisDto.setDenominator(rs.getString(BivariateAxisColumns.denominator.name()));
                bivariativeAxisDto.setNumeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()));
                bivariativeAxisDto.setDenominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name()));
                axisDtos.add(bivariativeAxisDto);

                List<AdvancedAnalyticsValues> valuesList = createValuesList(rs);
                advancedAnalyticsValues.add(valuesList);
            }));
        } catch (Exception e) {
            String error = String.format("Can't get value from result set %s", e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }

        List<AdvancedAnalyticsQualitySortDto> qualitySortedList = createSortedList(axisDtos, advancedAnalyticsValues);
        return getAdvancedAnalyticsResult(qualitySortedList, axisDtos, advancedAnalyticsValues);
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

    public List<AdvancedAnalyticsQualitySortDto> createSortedList(List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues) {
        List<AdvancedAnalyticsQualitySortDto> returnList = new ArrayList<>();

        for (int i = 0; i < argAxis.size(); i++) {
            AdvancedAnalyticsQualitySortDto valuesWithMinQuality = new AdvancedAnalyticsQualitySortDto();
            valuesWithMinQuality.setNumerator(argAxis.get(i).getNumerator());
            valuesWithMinQuality.setDenominator(argAxis.get(i).getDenominator());
            List<AdvancedAnalyticsValues> advancedAnalyticsValues = argValues.get(i);
            Double minQuality = null;
            for (AdvancedAnalyticsValues value : advancedAnalyticsValues) {
                Double quality = value.getQuality();
                if (minQuality == null && quality != null) {
                    minQuality = Math.abs(quality);
                } else if (minQuality != null && quality != null && quality < minQuality) {
                    minQuality = Math.abs(quality);
                }
            }

            valuesWithMinQuality.setMinQuality(minQuality);
            returnList.add(valuesWithMinQuality);
        }
        return returnList.stream()
                .sorted(Comparator.comparing(AdvancedAnalyticsQualitySortDto::getMinQuality, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<AdvancedAnalytics> getAdvancedAnalyticsResult(List<AdvancedAnalyticsQualitySortDto> argQualitySortedList, List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues) {
        List<AdvancedAnalytics> returnList = new ArrayList<>();

        for (int i = 0; i < argAxis.size(); i++) {
            AdvancedAnalytics analytics = new AdvancedAnalytics();
            analytics.setNumerator(argAxis.get(i).getNumerator());
            analytics.setDenominator(argAxis.get(i).getDenominator());
            analytics.setNumeratorLabel(argAxis.get(i).getNumeratorLabel());
            analytics.setDenominatorLabel(argAxis.get(i).getDenominatorLabel());
            analytics.setAnalytics(argValues.get(i));
            //get order according to quality value
            analytics.setOrder(getIndexOfListByQuality(argQualitySortedList, argAxis.get(i).getNumerator(), argAxis.get(i).getDenominator()));
            returnList.add(analytics);
        }

        //sort by quality order
        return returnList.stream()
                .sorted(Comparator.comparing(AdvancedAnalytics::getOrder))
                .toList();
    }

    private Integer getIndexOfListByQuality(List<AdvancedAnalyticsQualitySortDto> argQualitySortedList, String argNumerator, String argDenominator){
        OptionalInt indexOpt = IntStream.range(0, argQualitySortedList.size())
                .filter(i -> argNumerator.equals(argQualitySortedList.get(i).getNumerator()) && argDenominator.equals(argQualitySortedList.get(i).getDenominator()))
                .findFirst();

        return indexOpt.getAsInt();
    }
}