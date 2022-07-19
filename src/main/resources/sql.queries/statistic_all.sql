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
                                      'x', jsonb_build_object('label', ax.label, 'quotients',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('name', bix1.param_id, 'label', bix1.param_label, 'direction', bix1.direction),
                                                                      jsonb_build_object('name', bix2.param_id, 'label', bix2.param_label, 'direction', bix2.direction)),
                                                              'steps',
                                                              jsonb_build_array(
                                                                  jsonb_build_object('value', ax.min, 'label', ax.min_label),
                                                                  jsonb_build_object('value', ax.p25, 'label', ax.p25_label),
                                                                  jsonb_build_object('value', ax.p75, 'label', ax.p75_label),
                                                                  jsonb_build_object('value', ax.max, 'label', ax.max_label))),
                                      'y', jsonb_build_object('label', ay.label, 'quotients',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('name', biy1.param_id, 'label', biy1.param_label, 'direction', biy1.direction),
                                                                      jsonb_build_object('name', biy2.param_id, 'label', biy2.param_label, 'direction', biy2.direction)),
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
          bivariate_overlays o,
          bivariate_indicators bix1,
          bivariate_indicators bix2,
          bivariate_indicators biy1,
          bivariate_indicators biy2
      where
            bix1.param_id = o.x_numerator
        and bix2.param_id = o.x_denominator
        and biy1.param_id = o.y_numerator
        and biy2.param_id = o.y_denominator
        and ax.denominator = o.x_denominator
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
