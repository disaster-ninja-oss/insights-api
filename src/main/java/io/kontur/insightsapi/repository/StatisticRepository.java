package io.kontur.insightsapi.repository;

import com.google.common.collect.Lists;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.mapper.*;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.model.Statistic;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class StatisticRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final StatisticRowMapper statisticRowMapper;

    private final BivariateStatisticRowMapper bivariateStatisticRowMapper;

    private final AxisRowMapper axisRowMapper;

    private final PolygonCorrelationRateRowMapper polygonCorrelationRateRowMapper;

    private final CorrelationRateRowMapper correlationRateRowMapper;

    private final Logger logger = LoggerFactory.getLogger(StatisticRepository.class);

    @Transactional(readOnly = true)
    public Statistic getAllStatistic() {
        var query = """
                select
                        jsonb_build_object(
                                           'axis', ba.axis,
                                           'meta', jsonb_build_object('max_zoom', 8,
                                                                      'min_zoom', 0),
                                           'indicators', (
                                               select jsonb_agg(jsonb_build_object('name', param_id,
                                                                                   'label', param_label,
                                                                                   'direction', direction,
                                                                                   'copyrights', copyrights))
                                               from bivariate_indicators
                                           ),
                                           'colors', jsonb_build_object(
                                               'fallback', '#ccc',
                                               'combinations', (
                                                   select jsonb_agg(jsonb_build_object('color', color,
                                                                                                   'color_comment', color_comment,
                                                                                                   'corner', corner))
                                                   from bivariate_colors)
                                            ),
                                           'correlationRates', (
                                               select
                                                   jsonb_agg(jsonb_build_object(
                                                                 'x', jsonb_build_object('label', xcopy.param_label,
                                                                                         'quotient',
                                                                                         jsonb_build_array(x_num, x_den)),
                                                                 'y', jsonb_build_object('label', ycopy.param_label,
                                                                                         'quotient',
                                                                                         jsonb_build_array(y_num, y_den)),
                                                                 'rate', correlation,
                                                                 'correlation', correlation,
                                                                 'quality', quality,
                                                                 'avgCorrelationX', avg(abs(correlation)) over (partition by x_num, x_den),
                                                                 'avgCorrelationY', avg(abs(correlation)) over (partition by y_num, y_den)
                                                                 ),
                                                                 avg(abs(correlation)) over (partition by x_num, x_den) * avg(abs(correlation)) over (partition by y_num, y_den) mult
                                                             order by mult desc)
                                               from
                                                   bivariate_axis_correlation, bivariate_indicators xcopy, bivariate_indicators ycopy
                                               where xcopy.param_id = x_num and ycopy.param_id = y_num
                                           ),
                                           'initAxis',
                                           jsonb_build_object('x', jsonb_build_object('label', x.label, 'quotient',
                                                                                      jsonb_build_array(x.numerator, x.denominator),
                                                                                      'steps',
                                                                                      jsonb_build_array(
                                                                                          jsonb_build_object('value', x.min, 'label', x.min_label),
                                                                                          jsonb_build_object('value', x.p25, 'label', x.p25_label),
                                                                                          jsonb_build_object('value', x.p75, 'label', x.p75_label),
                                                                                          jsonb_build_object('value', x.max, 'label', x.max_label))),
                                                              'y', jsonb_build_object('label', y.label, 'quotient',
                                                                                      jsonb_build_array(y.numerator, y.denominator),
                                                                                      'steps',
                                                                                      jsonb_build_array(
                                                                                          jsonb_build_object('value', y.min, 'label', y.min_label),
                                                                                          jsonb_build_object('value', y.p25, 'label', y.p25_label),
                                                                                          jsonb_build_object('value', y.p75, 'label', y.p75_label),
                                                                                          jsonb_build_object('value', y.max, 'label', y.max_label)))
                                               ),
                                           'overlays', ov.overlay
                            )::text
                    from
                        ( select
                              json_agg(
                                  jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality',
                                                     quality,
                                                     'steps', jsonb_build_array(
                                                         jsonb_build_object('value', min, 'label', min_label),
                                                         jsonb_build_object('value', p25, 'label', p25_label),
                                                         jsonb_build_object('value', p75, 'label', p75_label),
                                                         jsonb_build_object('value', max, 'label', max_label)))) as axis
                          from
                              bivariate_axis )                                                                      ba,
                        ( select
                              json_agg(jsonb_build_object('name', o.name, 'active', o.active, 'description', o.description,
                                                          'colors', o.colors, 'order', o.ord,
                                                          'x', jsonb_build_object('label', ax.label, 'quotient',
                                                                                  jsonb_build_array(ax.numerator, ax.denominator),
                                                                                  'steps',
                                                                                  jsonb_build_array(
                                                                                      jsonb_build_object('value', ax.min, 'label', ax.min_label),
                                                                                      jsonb_build_object('value', ax.p25, 'label', ax.p25_label),
                                                                                      jsonb_build_object('value', ax.p75, 'label', ax.p75_label),
                                                                                      jsonb_build_object('value', ax.max, 'label', ax.max_label))),
                                                          'y', jsonb_build_object('label', ay.label, 'quotient',
                                                                                  jsonb_build_array(ay.numerator, ay.denominator),
                                                                                  'steps',
                                                                                  jsonb_build_array(
                                                                                      jsonb_build_object('value', ay.min, 'label', ay.min_label),
                                                                                      jsonb_build_object('value', ay.p25, 'label', ay.p25_label),
                                                                                      jsonb_build_object('value', ay.p75, 'label', ay.p75_label),
                                                                                      jsonb_build_object('value', ay.max, 'label', ay.max_label))))
                                       order by ord) as overlay
                          from
                              bivariate_axis     ax,
                              bivariate_axis     ay,
                              bivariate_overlays o
                          where
                                ax.denominator = o.x_denominator
                            and ax.numerator = o.x_numerator
                            and ay.denominator = o.y_denominator
                            and ay.numerator = o.y_numerator )                                                      ov,
                        bivariate_axis                                                                              x,
                        bivariate_axis                                                                              y
                    where
                          x.numerator = 'count'
                      and x.denominator = 'area_km2'
                      and y.numerator = 'view_count'
                      and y.denominator = 'area_km2'
                """.trim();
        return jdbcTemplate.queryForObject(query, statisticRowMapper);
    }

    @Transactional(readOnly = true)
    public BivariateStatistic getBivariateStatistic() {
        var query = """
                select
                        jsonb_build_object(
                                           'meta', jsonb_build_object('max_zoom', 8,
                                                                      'min_zoom', 0),
                                           'indicators', (
                                               select jsonb_agg(jsonb_build_object('name', param_id,
                                                                                   'label', param_label,
                                                                                   'direction', direction,
                                                                                   'copyrights', copyrights))
                                               from bivariate_indicators
                                           ),
                                           'colors', jsonb_build_object(
                                               'fallback', '#ccc',
                                               'combinations', (
                                                   select jsonb_agg(jsonb_build_object('color', color,
                                                                                                   'color_comment', color_comment,
                                                                                                   'corner', corner))
                                                   from bivariate_colors)
                                            ),
                                           'initAxis',
                                           jsonb_build_object('x', jsonb_build_object('label', x.label, 'quotient',
                                                                                      jsonb_build_array(x.numerator, x.denominator),
                                                                                      'steps',
                                                                                      jsonb_build_array(
                                                                                          jsonb_build_object('value', x.min, 'label', x.min_label),
                                                                                          jsonb_build_object('value', x.p25, 'label', x.p25_label),
                                                                                          jsonb_build_object('value', x.p75, 'label', x.p75_label),
                                                                                          jsonb_build_object('value', x.max, 'label', x.max_label))),
                                                              'y', jsonb_build_object('label', y.label, 'quotient',
                                                                                      jsonb_build_array(y.numerator, y.denominator),
                                                                                      'steps',
                                                                                      jsonb_build_array(
                                                                                          jsonb_build_object('value', y.min, 'label', y.min_label),
                                                                                          jsonb_build_object('value', y.p25, 'label', y.p25_label),
                                                                                          jsonb_build_object('value', y.p75, 'label', y.p75_label),
                                                                                          jsonb_build_object('value', y.max, 'label', y.max_label)))
                                               ),
                                           'overlays', ov.overlay
                            )::text
                    from
                        ( select
                              json_agg(
                                  jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality',
                                                     quality,
                                                     'steps', jsonb_build_array(
                                                         jsonb_build_object('value', min, 'label', min_label),
                                                         jsonb_build_object('value', p25, 'label', p25_label),
                                                         jsonb_build_object('value', p75, 'label', p75_label),
                                                         jsonb_build_object('value', max, 'label', max_label)))) as axis
                          from
                              bivariate_axis )                                                                      ba,
                        ( select
                              json_agg(jsonb_build_object('name', o.name, 'active', o.active, 'description', o.description,
                                                          'colors', o.colors, 'order', o.ord,
                                                          'x', jsonb_build_object('label', ax.label, 'quotient',
                                                                                  jsonb_build_array(ax.numerator, ax.denominator),
                                                                                  'steps',
                                                                                  jsonb_build_array(
                                                                                      jsonb_build_object('value', ax.min, 'label', ax.min_label),
                                                                                      jsonb_build_object('value', ax.p25, 'label', ax.p25_label),
                                                                                      jsonb_build_object('value', ax.p75, 'label', ax.p75_label),
                                                                                      jsonb_build_object('value', ax.max, 'label', ax.max_label))),
                                                          'y', jsonb_build_object('label', ay.label, 'quotient',
                                                                                  jsonb_build_array(ay.numerator, ay.denominator),
                                                                                  'steps',
                                                                                  jsonb_build_array(
                                                                                      jsonb_build_object('value', ay.min, 'label', ay.min_label),
                                                                                      jsonb_build_object('value', ay.p25, 'label', ay.p25_label),
                                                                                      jsonb_build_object('value', ay.p75, 'label', ay.p75_label),
                                                                                      jsonb_build_object('value', ay.max, 'label', ay.max_label))))
                                       order by ord) as overlay
                          from
                              bivariate_axis     ax,
                              bivariate_axis     ay,
                              bivariate_overlays o
                          where
                                ax.denominator = o.x_denominator
                            and ax.numerator = o.x_numerator
                            and ay.denominator = o.y_denominator
                            and ay.numerator = o.y_numerator )                                                      ov,
                        bivariate_axis                                                                              x,
                        bivariate_axis                                                                              y
                    where
                          x.numerator = 'count'
                      and x.denominator = 'area_km2'
                      and y.numerator = 'view_count'
                      and y.denominator = 'area_km2'
                """.trim();
        return jdbcTemplate.queryForObject(query, bivariateStatisticRowMapper);
    }

    @Transactional(readOnly = true)
    public List<Axis> getAxisStatistic() {
        var query = """
                select
                            jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality',
                                               quality,
                                               'steps', jsonb_build_array(
                                                       jsonb_build_object('value', min, 'label', min_label),
                                                       jsonb_build_object('value', p25, 'label', p25_label),
                                                       jsonb_build_object('value', p75, 'label', p75_label),
                                                       jsonb_build_object('value', max, 'label', max_label)))
                   from bivariate_axis
                """.trim();
        return jdbcTemplate.query(query, axisRowMapper);
    }

    @Transactional(readOnly = true)
    public List<PolygonCorrelationRate> getAllCorrelationRateStatistics() {
        var query = """
                select
                    jsonb_build_object(
                                      'x', jsonb_build_object('label', xcopy.param_label,
                                                              'quotient',
                                                              jsonb_build_array(x_num, x_den)),
                                      'y', jsonb_build_object('label', ycopy.param_label,
                                                              'quotient',
                                                              jsonb_build_array(y_num, y_den)),
                                      'rate', correlation,
                                      'correlation', correlation,
                                      'quality', quality,
                                      'avgCorrelationX', avg(abs(correlation)) over (partition by x_num, x_den),
                                      'avgCorrelationY', avg(abs(correlation)) over (partition by y_num, y_den)
                                  ),
                    avg(abs(correlation)) over (partition by x_num, x_den) * avg(abs(correlation)) over (partition by y_num, y_den) mult
                from
                    bivariate_axis_correlation, bivariate_indicators xcopy, bivariate_indicators ycopy
                    where xcopy.param_id = x_num and ycopy.param_id = y_num
                    order by mult desc
                """.trim();
        return jdbcTemplate.query(query, polygonCorrelationRateRowMapper);
    }

    @Transactional(readOnly = true)
    public List<NumeratorsDenominatorsDto> getNumeratorsDenominatorsForCorrelation() {
        var query = """
                select x.numerator                             as x_num,
                           x.denominator                           as x_den,
                           x_den_indicator.param_label             as x_param_label,
                           y.numerator                             as y_num,
                           y.denominator                           as y_den,
                           y_den_indicator.param_label             as y_param_label,
                           1 - ((1 - x.quality) * (1 - y.quality)) as quality
                    from (bivariate_axis x
                             join bivariate_indicators x_den_indicator
                                  on (x.denominator = x_den_indicator.param_id)
                             join bivariate_indicators x_num_indicator
                                  on (x.numerator = x_num_indicator.param_id)),
                         (bivariate_axis y
                             join bivariate_indicators y_den_indicator
                                  on (y.denominator = y_den_indicator.param_id))
                    where (x.numerator != y.numerator)
                      and x.quality > 0.5
                      and y.quality > 0.5
                      and x_den_indicator.is_base
                      and y_den_indicator.is_base
                      and not x_num_indicator.is_base
                """.trim();
        return jdbcTemplate.query(query, (rs, rowNum) ->
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
    public List<Double> getPolygonCorrelationRateStatisticsBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", polygon);
        var requests = dtoList.stream()
                .map(dto -> createCorrelationQueryString(dto.getXNumerator(), dto.getXDenominator(),
                        dto.getYNumerator(), dto.getYDenominator())).collect(Collectors.toList());
        var distinctFieldsRequests = dtoList.stream()
                .flatMap(dto -> Stream.of(dto.getXNumerator(), dto.getYNumerator(), dto.getXDenominator(), dto.getYDenominator()))
                .distinct()
                .collect(Collectors.toList());
        var query = String.format("""
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
                                          select h3, %s
                                          from stat_h3 sh
                                          where ST_Intersects(sh.geom, p.geom)
                                            order by h3
                                          ) h
                     )
                select %s from stat_area
                """.trim(), StringUtils.join(distinctFieldsRequests, ","), StringUtils.join(requests, ","));
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
    public Map<String, Boolean> getNumeratorsForNotEmptyLayersBatch(List<NumeratorsDenominatorsDto> dtoList, String polygon){
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
        var query = String.format("""
                with validated_input as (
                    select calculate_validated_input(:polygon) geom
                ),
                     stat_area as (
                                 select %s
                                 from (
                                          select ST_Subdivide(v.geom, 30) geom
                                          from validated_input v
                                      ) p
                                          cross join
                                      lateral (
                                          select h3, %s
                                          from stat_h3 sh
                                          where ST_Intersects(sh.geom, p.geom)
                                          ) h
                     )
                select * from stat_area
                """.trim(), StringUtils.join(requests, ","), StringUtils.join(distinctFieldsRequests, ","));
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

    public void jitDisable() {
        jdbcTemplate.execute("set local jit = off");
    }
}
