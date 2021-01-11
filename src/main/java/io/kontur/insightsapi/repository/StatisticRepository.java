package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.mapper.StatisticRowMapper;
import io.kontur.insightsapi.model.Statistic;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class StatisticRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final StatisticRowMapper statisticRowMapper;

    @Transactional(readOnly = true)
    public Statistic getAllStatistic() {
        var query = "select" +
                "        jsonb_build_object('axis', ba.axis," +
                "                           'meta', jsonb_build_object('maxZoom', 8," +
                "                                                      'minZoom', 0)," +
                "                           'indicators', (" +
                "                               select jsonb_agg(jsonb_build_object('name', param_id," +
                "                                                                   'label', param_label," +
                "                                                                   'copyrights', copyrights))" +
                "                               from bivariate_indicators" +
                "                           )," +
                "                           'correlationRates', (" +
                "                               select" +
                "                                   jsonb_agg(jsonb_build_object(" +
                "                                                 'x', jsonb_build_object('label', xcopy.param_label," +
                "                                                                         'quotient'," +
                "                                                                         jsonb_build_array(x_num, x_den))," +
                "                                                 'y', jsonb_build_object('label', ycopy.param_label," +
                "                                                                         'quotient'," +
                "                                                                         jsonb_build_array(y_num, y_den))," +
                "                                                 'rate', correlation," +
                "                                                 'correlation', correlation," +
                "                                                 'quality', quality" +
                "                                                 )" +
                "                                             order by abs(correlation) * quality nulls last, abs(correlation) desc)" +
                "                               from" +
                "                                   bivariate_axis_correlation, bivariate_indicators xcopy, bivariate_indicators ycopy" +
                "                               where xcopy.param_id = x_num and ycopy.param_id = y_num" +
                "                           )," +
                "                           'initAxis'," +
                "                           jsonb_build_object('x', jsonb_build_object('label', x.label, 'quotient'," +
                "                                                                      jsonb_build_array(x.numerator, x.denominator)," +
                "                                                                      'steps'," +
                "                                                                      jsonb_build_array(" +
                "                                                                          jsonb_build_object('value', x.min, 'label', x.min_label)," +
                "                                                                          jsonb_build_object('value', x.p25, 'label', x.p25_label)," +
                "                                                                          jsonb_build_object('value', x.p75, 'label', x.p75_label)," +
                "                                                                          jsonb_build_object('value', x.max, 'label', x.max_label)))," +
                "                                              'y', jsonb_build_object('label', y.label, 'quotient'," +
                "                                                                      jsonb_build_array(y.numerator, y.denominator)," +
                "                                                                      'steps'," +
                "                                                                      jsonb_build_array(" +
                "                                                                          jsonb_build_object('value', y.min, 'label', y.min_label)," +
                "                                                                          jsonb_build_object('value', y.p25, 'label', y.p25_label)," +
                "                                                                          jsonb_build_object('value', y.p75, 'label', y.p75_label)," +
                "                                                                          jsonb_build_object('value', y.max, 'label', y.max_label)))" +
                "                               )," +
                "                           'overlays', ov.overlay" +
                "            )::text" +
                "    from" +
                "        ( select" +
                "              json_agg(" +
                "                  jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality'," +
                "                                     quality," +
                "                                     'steps', jsonb_build_array(" +
                "                                         jsonb_build_object('value', min, 'label', min_label)," +
                "                                         jsonb_build_object('value', p25, 'label', p25_label)," +
                "                                         jsonb_build_object('value', p75, 'label', p75_label)," +
                "                                         jsonb_build_object('value', max, 'label', max_label)))) as axis" +
                "          from" +
                "              bivariate_axis )                                                                      ba," +
                "        ( select" +
                "              json_agg(jsonb_build_object('name', o.name, 'active', o.active, 'description', o.description," +
                "                                          'colors', o.colors," +
                "                                          'x', jsonb_build_object('label', ax.label, 'quotient'," +
                "                                                                  jsonb_build_array(ax.numerator, ax.denominator)," +
                "                                                                  'steps'," +
                "                                                                  jsonb_build_array(" +
                "                                                                      jsonb_build_object('value', ax.min, 'label', ax.min_label)," +
                "                                                                      jsonb_build_object('value', ax.p25, 'label', ax.p25_label)," +
                "                                                                      jsonb_build_object('value', ax.p75, 'label', ax.p75_label)," +
                "                                                                      jsonb_build_object('value', ax.max, 'label', ax.max_label)))," +
                "                                          'y', jsonb_build_object('label', ay.label, 'quotient'," +
                "                                                                  jsonb_build_array(ay.numerator, ay.denominator)," +
                "                                                                  'steps'," +
                "                                                                  jsonb_build_array(" +
                "                                                                      jsonb_build_object('value', ay.min, 'label', ay.min_label)," +
                "                                                                      jsonb_build_object('value', ay.p25, 'label', ay.p25_label)," +
                "                                                                      jsonb_build_object('value', ay.p75, 'label', ay.p75_label)," +
                "                                                                      jsonb_build_object('value', ay.max, 'label', ay.max_label))))" +
                "                       order by ord) as overlay" +
                "          from" +
                "              bivariate_axis     ax," +
                "              bivariate_axis     ay," +
                "              bivariate_overlays o" +
                "          where" +
                "                ax.denominator = o.x_denominator" +
                "            and ax.numerator = o.x_numerator" +
                "            and ay.denominator = o.y_denominator" +
                "            and ay.numerator = o.y_numerator )                                                      ov," +
                "        bivariate_axis                                                                              x," +
                "        bivariate_axis                                                                              y" +
                "    where" +
                "          x.numerator = 'count'" +
                "      and x.denominator = 'area_km2'" +
                "      and y.numerator = 'view_count'" +
                "      and y.denominator = 'area_km2'";
        return jdbcTemplate.queryForObject(query, statisticRowMapper);
    }

    @Transactional(readOnly = true)
    public Statistic getPolygonStatistic(PolygonStatisticRequest request) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", request.getPolygon());
        paramSource.addValue("xNumerator", request.getXNumeratorList());
        paramSource.addValue("yNumerator", request.getYNumeratorList());
        var query =
                "with bivariate_axis_correlation_polygon as (" +
                        "       select x.numerator as x_num, x.denominator as x_den, y.numerator as y_num, y.denominator as y_den," +
                        "       correlate_bivariate_axes(:polygon, x.numerator, x.denominator, y.numerator, y.denominator) as correlation," +
                        "       1 - ((1 - x.quality) * (1 - y.quality)) as quality" +
                        "       from" +
                        "        (bivariate_axis x" +
                        "            join bivariate_indicators x_den_indicator" +
                        "                    on (x.denominator = x_den_indicator.param_id)" +
                        "            join bivariate_indicators x_num_indicator" +
                        "                    on (x.numerator = x_num_indicator.param_id))," +
                        "            (bivariate_axis y" +
                        "                join bivariate_indicators y_den_indicator" +
                        "                    on (y.denominator = y_den_indicator.param_id))" +
                        "    where" +
                        "          (x.numerator != y.numerator or x.denominator != y.denominator)" +
                        "      and x.numerator in (:xNumerator)" +
                        "      and y.numerator in (:yNumerator)" +
                        "      and x.quality > 0.5" +
                        "      and y.quality > 0.5" +
                        "      and x_den_indicator.is_base" +
                        "      and y_den_indicator.is_base" +
                        "      and not x_num_indicator.is_base" +
                        ")" +
                        "select" +
                        "        jsonb_build_object('axis', ba.axis," +
                        "                           'meta', jsonb_build_object('maxZoom', 8," +
                        "                                                      'minZoom', 0)," +
                        "                           'indicators', (" +
                        "                               select jsonb_agg(jsonb_build_object('name', param_id," +
                        "                                                                   'label', param_label," +
                        "                                                                   'copyrights', copyrights))" +
                        "                               from bivariate_indicators" +
                        "                           )," +
                        "                           'correlationRates', (" +
                        "                               select" +
                        "                                   jsonb_agg(jsonb_build_object(" +
                        "                                                 'x', jsonb_build_object('label', xcopy.param_label," +
                        "                                                                         'quotient'," +
                        "                                                                         jsonb_build_array(x_num, x_den))," +
                        "                                                 'y', jsonb_build_object('label', ycopy.param_label," +
                        "                                                                         'quotient'," +
                        "                                                                         jsonb_build_array(y_num, y_den))," +
                        "                                                 'rate', correlation," +
                        "                                                 'correlation', correlation," +
                        "                                                 'quality', quality" +
                        "                                                 )" +
                        "                                             order by abs(correlation) * quality nulls last, abs(correlation) desc)" +
                        "                               from" +
                        "                                   bivariate_axis_correlation_polygon, bivariate_indicators xcopy, bivariate_indicators ycopy" +
                        "                               where xcopy.param_id = x_num and ycopy.param_id = y_num" +
                        "                           )," +
                        "                           'initAxis'," +
                        "                           jsonb_build_object('x', jsonb_build_object('label', x.label, 'quotient'," +
                        "                                                                      jsonb_build_array(x.numerator, x.denominator)," +
                        "                                                                      'steps'," +
                        "                                                                      jsonb_build_array(" +
                        "                                                                          jsonb_build_object('value', x.min, 'label', x.min_label)," +
                        "                                                                          jsonb_build_object('value', x.p25, 'label', x.p25_label)," +
                        "                                                                          jsonb_build_object('value', x.p75, 'label', x.p75_label)," +
                        "                                                                          jsonb_build_object('value', x.max, 'label', x.max_label)))," +
                        "                                              'y', jsonb_build_object('label', y.label, 'quotient'," +
                        "                                                                      jsonb_build_array(y.numerator, y.denominator)," +
                        "                                                                      'steps'," +
                        "                                                                      jsonb_build_array(" +
                        "                                                                          jsonb_build_object('value', y.min, 'label', y.min_label)," +
                        "                                                                          jsonb_build_object('value', y.p25, 'label', y.p25_label)," +
                        "                                                                          jsonb_build_object('value', y.p75, 'label', y.p75_label)," +
                        "                                                                          jsonb_build_object('value', y.max, 'label', y.max_label)))" +
                        "                               )," +
                        "                           'overlays', ov.overlay" +
                        "            )::text" +
                        "    from" +
                        "        ( select" +
                        "              json_agg(" +
                        "                  jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality'," +
                        "                                     quality," +
                        "                                     'steps', jsonb_build_array(" +
                        "                                         jsonb_build_object('value', min, 'label', min_label)," +
                        "                                         jsonb_build_object('value', p25, 'label', p25_label)," +
                        "                                         jsonb_build_object('value', p75, 'label', p75_label)," +
                        "                                         jsonb_build_object('value', max, 'label', max_label)))) as axis" +
                        "          from" +
                        "              bivariate_axis )                                                                      ba," +
                        "        ( select" +
                        "              json_agg(jsonb_build_object('name', o.name, 'active', o.active, 'description', o.description," +
                        "                                          'colors', o.colors," +
                        "                                          'x', jsonb_build_object('label', ax.label, 'quotient'," +
                        "                                                                  jsonb_build_array(ax.numerator, ax.denominator)," +
                        "                                                                  'steps'," +
                        "                                                                  jsonb_build_array(" +
                        "                                                                      jsonb_build_object('value', ax.min, 'label', ax.min_label)," +
                        "                                                                      jsonb_build_object('value', ax.p25, 'label', ax.p25_label)," +
                        "                                                                      jsonb_build_object('value', ax.p75, 'label', ax.p75_label)," +
                        "                                                                      jsonb_build_object('value', ax.max, 'label', ax.max_label)))," +
                        "                                          'y', jsonb_build_object('label', ay.label, 'quotient'," +
                        "                                                                  jsonb_build_array(ay.numerator, ay.denominator)," +
                        "                                                                  'steps'," +
                        "                                                                  jsonb_build_array(" +
                        "                                                                      jsonb_build_object('value', ay.min, 'label', ay.min_label)," +
                        "                                                                      jsonb_build_object('value', ay.p25, 'label', ay.p25_label)," +
                        "                                                                      jsonb_build_object('value', ay.p75, 'label', ay.p75_label)," +
                        "                                                                      jsonb_build_object('value', ay.max, 'label', ay.max_label))))" +
                        "                       order by ord) as overlay" +
                        "          from" +
                        "              bivariate_axis     ax," +
                        "              bivariate_axis     ay," +
                        "              bivariate_overlays o" +
                        "          where" +
                        "                ax.denominator = o.x_denominator" +
                        "            and ax.numerator = o.x_numerator" +
                        "            and ay.denominator = o.y_denominator" +
                        "            and ay.numerator = o.y_numerator )                                                      ov," +
                        "        bivariate_axis                                                                              x," +
                        "        bivariate_axis                                                                              y" +
                        "    where" +
                        "          x.numerator = 'count'" +
                        "      and x.denominator = 'area_km2'" +
                        "      and y.numerator = 'view_count'" +
                        "      and y.denominator = 'area_km2'";
        return namedParameterJdbcTemplate.queryForObject(query, paramSource, statisticRowMapper);
    }
}
