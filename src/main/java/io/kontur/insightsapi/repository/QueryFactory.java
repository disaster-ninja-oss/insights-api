package io.kontur.insightsapi.repository;

public class QueryFactory {
    public static String getBivariativeAxis_query() {
        return """
                select ax.numerator, ax.denominator, ind1.param_label numerator_label,
                   ind2.param_label denominator_label
                   from bivariate_axis ax
                   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
                   join bivariate_indicators ind2 on ind2.param_id = ax.denominator
                """.trim();
    }

    public static String getQueryWithGeom_query() {
        return """
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
                     """.trim();
    }

    public static String getUnionQuery_query() {
        return """
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
                """.trim();
    }

    public static String getWorldData_query() {
        return """
                select ax.numerator, ax.denominator, ind1.param_label numerator_label,
                   ind2.param_label denominator_label,
                   ax.sum_value, ax.sum_quality, ax.min_value, ax.min_quality,
                   ax.max_value, ax.max_quality, ax.stddev_value, ax.stddev_quality,
                   ax.median_value, ax.median_quality, ax.mean_value, ax.mean_quality
                   from bivariate_axis ax
                   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
                   join bivariate_indicators ind2 on ind2.param_id = ax.denominator
                """.trim();
    }

    public static String calculateFunctionsResult_query() {
        return """
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
                            select h3,
                                   population,
                                   populated_area_km2,
                                   count,
                                   building_count,
                                   highway_length,
                                   industrial_area,
                                   wildfires,
                                   volcanos_count,
                                   forest
                            from stat_h3 sh
                            where ST_Intersects(sh.geom, p.geom)
                                and sh.zoom = 8
                                order by h3
                        ) h
                )
                select %s from stat_area st
                """.trim();
    }

    public static String getAllStatistic_query(){
        return """
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
                                                          'colors', o.colors,
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
    }

    public static String getBivariateStatistic_query(){
        return """
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
                                                          'colors', o.colors,
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
    }

    public static String getAxisStatistic_query(){
        return """
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
    }

    public static String getAllCorrelationRateStatistics_query(){
        return """
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
    }

    public static String getNumeratorsDenominatorsForCorrelation_query(){
        return """
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
    }

    public static String getPolygonCorrelationRateStatisticsBatch_query(){
        return """
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
                """.trim();
    }

    public static String getNumeratorsForNotEmptyLayersBatch_query(){
        return """
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
                """.trim();
    }

    public static String calculateThermalSpotStatistic_query(){
        return """
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
                                          select h3,
                                                 industrial_area,
                                                 wildfires,
                                                 volcanos_count,
                                                 forest
                                          from stat_h3 sh
                                          where ST_Intersects(sh.geom, p.geom)
                                            and sh.zoom = 8
                                          order by h3
                                          ) h
                             )
                        select %s from stat_area st
                """.trim();
    }

    public static String calculateHumanitarianImpact_query(){
        return """
                        with resolution as (
                            select calculate_area_resolution(ST_SetSRID(:geometry::geometry, 4326)) as resolution
                        ),
                             validated_input as (
                                select calculate_validated_input(:geometry) geom
                             ),
                            subdivided_polygons as (
                                      select ST_Subdivide(v.geom) geom
                                      from validated_input v
                                  ),
                             stat_in_area as (
                                 select s.*, sum(population) over (order by population desc) as sum_pop
                                 from (
                                          select distinct h3, population, area_km2, sh.geom
                                          from stat_h3 sh,
                                               subdivided_polygons p
                                          where zoom = (select resolution from resolution)
                                            and population > 0
                                            and ST_Intersects(
                                                  sh.geom,
                                                  p.geom
                                              )
                                      ) s
                             ),
                             total as (
                                 select sum(population) as population, round(sum(area_km2)::numeric, 2) as area from stat_in_area
                             )
                        select sum(s.population)                                as population,
                               case
                                   when sum_pop <= t.population * 0.68 then '0-68'
                                   else '68-100'
                                   end                                          as percentage,
                               case
                                   when sum_pop <= t.population * 0.68 then 'Kontur Urban Core'
                                   else 'Kontur Settled Periphery'
                                   end                                          as name,
                               round(sum(area_km2)::numeric, 2)                 as areaKm2,
                               ST_AsGeoJSON(ST_Transform(ST_Union(geom), 4326)) as geometry,
                               t.population                                     as totalPopulation,
                               t.area                                           as totalAreaKm2
                        from stat_in_area s,
                             total t
                        group by t.population, t.area, 2, 3
                """.trim();
    }

    public static String calculateOsmQuality_query(){
        return """
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
                                          select h3,
                                                 count,
                                                 building_count,
                                                 highway_length,
                                                 population,
                                                 populated_area_km2,
                                                 area_km2
                                          from stat_h3 sh
                                          where ST_Intersects(sh.geom, p.geom)
                                            and sh.zoom = 8
                                            and sh.population > 0
                                          order by h3
                                          ) h
                             )
                        select %s from stat_area st
                """.trim();
    }

    public static String calculateUrbanCore_query(){
        return """
                with resolution as (
                    select calculate_area_resolution(ST_SetSRID(:polygon::geometry, 4326)) as resolution
                ),
                                     validated_input as (
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
                                                  select h3,
                                                         population,
                                                         area_km2
                                                  from stat_h3 sh
                                                  where ST_Intersects(sh.geom, p.geom)
                                                    and sh.zoom = (select resolution from resolution)
                                                    and sh.population > 0
                                                  order by h3
                                                  ) h
                                     ),
                                     stat_pop as (
                                         select s.*, sum(population) over (order by population desc) as sum_pop
                                         from stat_area s
                                     ),
                                     total as (
                                         select sum(population)                  as population,
                                                round(sum(area_km2)::numeric, 2) as area
                                         from stat_pop
                                     )
                                select sum(s.population)                as urbanCorePopulation,
                                       round(sum(area_km2)::numeric, 2) as urbanCoreAreaKm2,
                                       t.area                           as totalPopulatedAreaKm2
                                from stat_pop s,
                                     total t
                                where sum_pop <= t.population * 0.68
                                group by t.population, t.area;
                        """.trim();
    }
}
