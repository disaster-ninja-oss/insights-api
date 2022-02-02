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
import java.sql.SQLException;
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
        var query = """
                select ax.numerator, ax.denominator, ind1.param_label numerator_label,
                   ind2.param_label denominator_label
                   from bivariate_axis ax
                   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
                   join bivariate_indicators ind2 on ind2.param_id = ax.denominator
                """.trim();
        return namedParameterJdbcTemplate.query(query, (rs, rowNum) -> BivariativeAxisDto.builder().numerator(rs.getString("numerator")).denominator(rs.getString("denominator")).numeratorLabel(rs.getString("numerator_label")).denominatorLabel(rs.getString("denominator_label")).build());
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
                 select avg(sum) filter (where r = 8) as sum,
                                       case
                                          when (nullif(max(sum),0) / nullif(min(sum), 0)) > 0
                                          then log10(nullif(max(sum), 0) / nullif(min(sum),0))
                                          else log10((nullif(max(sum), 0) - nullif(min(sum),0)) / least(abs(nullif(min(sum),0)), abs(nullif(max(sum),0))))
                                       end as sum_quality,
                                    avg(min) filter (where r = 8) as min,
                                       case
                                          when (nullif(max(min),0) / nullif(min(min), 0)) > 0
                                          then log10(nullif(max(min), 0) / nullif(min(min),0))
                                          else log10((nullif(max(min), 0) - nullif(min(min),0)) / least(abs(nullif(min(min),0)), abs(nullif(max(min),0))))
                                       end as min_quality,
                                    avg(max) filter (where r = 8) as max,
                                       case
                                          when (nullif(max(max),0) / nullif(min(max), 0)) > 0
                                          then log10(nullif(max(max), 0) / nullif(min(max),0))
                                          else log10((nullif(max(max), 0) - nullif(min(max),0)) / least(abs(nullif(min(max),0)), abs(nullif(max(max),0))))
                                       end as max_quality,
                                    avg(mean) filter (where r = 8) as mean,
                                       case
                                          when (nullif(max(mean),0) / nullif(min(mean), 0)) > 0
                                          then log10(nullif(max(mean), 0) / nullif(min(mean),0))
                                          else log10((nullif(max(mean), 0) - nullif(min(mean),0)) / least(abs(nullif(min(mean),0)), abs(nullif(max(mean),0))))
                                       end as mean_quality,
                                    avg(stddev) filter (where r = 8) as stddev,
                                       case
                                          when (nullif(max(stddev),0) / nullif(min(stddev), 0)) > 0
                                          then log10(nullif(max(stddev), 0) / nullif(min(stddev),0))
                                          else log10((nullif(max(stddev), 0) - nullif(min(stddev),0)) / least(abs(nullif(min(stddev),0)), abs(nullif(max(stddev),0))))
                                       end as stddev_quality,
                                    avg(median) filter (where r = 8) as median,
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
        return calculationsList.stream().map(arg -> {
            try {
                return new AdvancedAnalyticsValues(arg, rs.getDouble(arg), rs.getDouble(arg + "_quality"));
            } catch (SQLException e) {
                logger.error("Can't get value from result set", e);
                return null;
            }
        }).toList();
    }

    private enum Calculations {
        sum, min, max, mean, stddev, median
    }
}
