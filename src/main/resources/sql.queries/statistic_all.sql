select
    jsonb_build_object(
                       'axis', ba.axis,
                       'meta', jsonb_build_object('max_zoom', 8,
                                                  'min_zoom', 0),
                       'indicators', (
                           select jsonb_agg(jsonb_build_object('name', bi.param_id,
                                                               'label', bi.param_label,
                                                               'direction', bi.direction,
                                                               'copyrights', bi.copyrights,
                                                               'unit', jsonb_build_object('id', bul.unit_id,
                                                                                          'shortName', bul.short_name,
                                                                                          'longName', bul.long_name)))
                           from bivariate_indicators_metadata bi join bivariate_unit_localization bul on bi.unit_id = bul.unit_id
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
                                jsonb_agg(
                                    jsonb_build_object(
                                        'x', jsonb_build_object('label', x_label, 'quotient', jsonb_build_array(x_numerator_id, x_denominator_id)),
                                        'y', jsonb_build_object('label', y_label, 'quotient', jsonb_build_array(y_numerator_id, y_denominator_id)),
                                        'rate', correlation,
                                        'correlation', correlation,
                                        'quality', quality,
                                        'avgCorrelationX', avg_corr_x,
                                        'avgCorrelationY', avg_corr_y
                                    )
                                order by avg_corr_x * avg_corr_y desc)
                            from (
                                select
                                    xcopy.param_label as x_label,
                                    ycopy.param_label as y_label,
                                    x_numerator_id,
                                    x_denominator_id,
                                    y_numerator_id,
                                    y_denominator_id,
                                    correlation,
                                    quality,
                                    avg(abs(correlation)) over (partition by x_numerator_id, x_denominator_id) as avg_corr_x,
                                    avg(abs(correlation)) over (partition by y_numerator_id, y_denominator_id) as avg_corr_y
                                from
                                    bivariate_axis_correlation_v2
                                    join bivariate_indicators_metadata xcopy on xcopy.internal_id = x_numerator_id
                                    join bivariate_indicators_metadata ycopy on ycopy.internal_id = y_numerator_id
                                where
                                    correlation is not null
                      )),
                       'initAxis',
                       jsonb_build_object('x', jsonb_build_object('label', x.label, 'quotient',
                                                                  jsonb_build_array(x.numerator, x.denominator),
                                                                  'steps',
                                                                  jsonb_build_array(
                                                                      jsonb_build_object('value', floor(x.min), 'label', x.min_label),
                                                                      jsonb_build_object('value', x.p25, 'label', x.p25_label),
                                                                      jsonb_build_object('value', x.p75, 'label', x.p75_label),
                                                                      jsonb_build_object('value', ceil(x.max), 'label', x.max_label))),
                                          'y', jsonb_build_object('label', y.label, 'quotient',
                                                                  jsonb_build_array(y.numerator, y.denominator),
                                                                  'steps',
                                                                  jsonb_build_array(
                                                                      jsonb_build_object('value', floor(y.min), 'label', y.min_label),
                                                                      jsonb_build_object('value', y.p25, 'label', y.p25_label),
                                                                      jsonb_build_object('value', y.p75, 'label', y.p75_label),
                                                                      jsonb_build_object('value', ceil(y.max), 'label', y.max_label)))
                           )
        )::text
from
    ( select
          json_agg(
              jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality',
                                 quality,
                                 'steps', jsonb_build_array(
                                     jsonb_build_object('value', floor(min), 'label', min_label),
                                     jsonb_build_object('value', p25, 'label', p25_label),
                                     jsonb_build_object('value', p75, 'label', p75_label),
                                     jsonb_build_object('value', ceil(max), 'label', max_label)))) as axis
      from
          bivariate_axis_v2 )                                                                      ba,
    bivariate_axis_v2                                                                              x,
    bivariate_axis_v2                                                                              y
where
      x.numerator = 'count'
  and x.denominator = 'area_km2'
  and y.numerator = 'view_count'
  and y.denominator = 'area_km2'
