select
    jsonb_build_object(
                       'meta', jsonb_build_object('max_zoom', 8,
                                                  'min_zoom', 0),
                       'indicators', (
                           select jsonb_agg(jsonb_build_object('name', bi.param_id,
                                                               'label', bi.param_label,
                                                               'emoji', bi.emoji,
                                                               'direction', bi.direction,
                                                               'copyrights', bi.copyrights,
                                                               'description', bi.description,
                                                               'coverage', bi.coverage,
                                                               'update_frequency', bi.update_frequency,
                                                               'unit', jsonb_build_object('id', bul.unit_id,
                                                                                          'shortName', bul.short_name,
                                                                                          'longName', bul.long_name)))
                           from %s bi left join bivariate_unit_localization bul on bi.unit_id = bul.unit_id
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
                                                                  'quotients',
                                                                  jsonb_build_array(
                                                                          jsonb_build_object('name', bix1.param_id,
                                                                                             'label', bix1.param_label,
                                                                                             'emoji', bix1.emoji,
                                                                                             'direction', bix1.direction,
                                                                                             'description', bix1.description,
                                                                                             'coverage', bix1.coverage,
                                                                                             'update_frequency', bix1.update_frequency,
                                                                                             'unit', jsonb_build_object('id', bulx1.unit_id,
                                                                                                                        'shortName', bulx1.short_name,
                                                                                                                        'longName', bulx1.long_name)),
                                                                          jsonb_build_object('name', bix2.param_id,
                                                                                             'label', bix2.param_label,
                                                                                             'emoji', bix2.emoji,
                                                                                             'direction', bix2.direction,
                                                                                             'description', bix2.description,
                                                                                             'coverage', bix2.coverage,
                                                                                             'update_frequency', bix2.update_frequency,
                                                                                             'unit', jsonb_build_object('id', bulx2.unit_id,
                                                                                                                        'shortName', bulx2.short_name,
                                                                                                                        'longName', bulx2.long_name))
                                                                                   ),
                                                                  'steps',
                                                                  jsonb_build_array(
                                                                      jsonb_build_object('value', x.min, 'label', x.min_label),
                                                                      jsonb_build_object('value', x.p25, 'label', x.p25_label),
                                                                      jsonb_build_object('value', x.p75, 'label', x.p75_label),
                                                                      jsonb_build_object('value', x.max, 'label', x.max_label))),
                                          'y', jsonb_build_object('label', y.label, 'quotient',
                                                                  jsonb_build_array(y.numerator, y.denominator),
                                                                  'quotients',
                                                                  jsonb_build_array(
                                                                          jsonb_build_object('name', biy1.param_id,
                                                                                             'label', biy1.param_label,
                                                                                             'emoji', biy1.emoji,
                                                                                             'direction', biy1.direction,
                                                                                             'description', biy1.description,
                                                                                             'coverage', biy1.coverage,
                                                                                             'update_frequency', biy1.update_frequency,
                                                                                             'unit', jsonb_build_object('id', buly1.unit_id,
                                                                                                                        'shortName', buly1.short_name,
                                                                                                                        'longName', buly1.long_name)),
                                                                          jsonb_build_object('name', biy2.param_id,
                                                                                             'label', biy2.param_label,
                                                                                             'emoji', biy2.emoji,
                                                                                             'direction', biy2.direction,
                                                                                             'description', biy2.description,
                                                                                             'coverage', biy2.coverage,
                                                                                             'update_frequency', biy2.update_frequency,
                                                                                             'unit', jsonb_build_object('id', buly2.unit_id,
                                                                                                                        'shortName', buly2.short_name,
                                                                                                                        'longName', buly2.long_name))
                                                                                   ),
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
          %s )                                                                      ba,
    ( select
          json_agg(jsonb_build_object('name', o.name, 'active', o.active, 'description', o.description,
                                      'colors', o.colors, 'order', o.ord,
                                      'x', jsonb_build_object('label', ax.label,
                                                              'quotient', jsonb_build_array(ax.numerator, ax.denominator),
                                                              'quotients',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('name', bix1.param_id,
                                                                                         'label', bix1.param_label,
                                                                                         'emoji', bix1.emoji,
                                                                                         'direction', bix1.direction,
                                                                                         'description', bix1.description,
                                                                                         'coverage', bix1.coverage,
                                                                                         'update_frequency', bix1.update_frequency,
                                                                                         'unit', jsonb_build_object('id', bulx1.unit_id,
                                                                                                                    'shortName', bulx1.short_name,
                                                                                                                    'longName', bulx1.long_name)),
                                                                      jsonb_build_object('name', bix2.param_id,
                                                                                         'label', bix2.param_label,
                                                                                         'emoji', bix2.emoji,
                                                                                         'direction', bix2.direction,
                                                                                         'description', bix2.description,
                                                                                         'coverage', bix2.coverage,
                                                                                         'update_frequency', bix2.update_frequency,
                                                                                         'unit', jsonb_build_object('id', bulx2.unit_id,
                                                                                                                    'shortName', bulx2.short_name,
                                                                                                                    'longName', bulx2.long_name))
                                                                               ),
                                                              'steps',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('value', ax.min, 'label', ax.min_label),
                                                                      jsonb_build_object('value', ax.p25, 'label', ax.p25_label),
                                                                      jsonb_build_object('value', ax.p75, 'label', ax.p75_label),
                                                                      jsonb_build_object('value', ax.max, 'label', ax.max_label))),
                                      'y', jsonb_build_object('label', ay.label,
                                                              'quotient', jsonb_build_array(ay.numerator, ay.denominator),
                                                              'quotients',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('name', biy1.param_id,
                                                                                         'label', biy1.param_label,
                                                                                         'emoji', biy1.emoji,
                                                                                         'direction', biy1.direction,
                                                                                         'description', biy1.description,
                                                                                         'coverage', biy1.coverage,
                                                                                         'update_frequency', biy1.update_frequency,
                                                                                         'unit', jsonb_build_object('id', buly1.unit_id,
                                                                                                                    'shortName', buly1.short_name,
                                                                                                                    'longName', buly1.long_name)),
                                                                      jsonb_build_object('name', biy2.param_id,
                                                                                         'label', biy2.param_label,
                                                                                         'emoji', biy2.emoji,
                                                                                         'direction', biy2.direction,
                                                                                         'description', biy2.description,
                                                                                         'coverage', biy2.coverage,
                                                                                         'update_frequency', biy2.update_frequency,
                                                                                         'unit', jsonb_build_object('id', buly2.unit_id,
                                                                                                                    'shortName', buly2.short_name,
                                                                                                                    'longName', buly2.long_name))
                                                                               ),
                                                              'steps',
                                                              jsonb_build_array(
                                                                      jsonb_build_object('value', ay.min, 'label', ay.min_label),
                                                                      jsonb_build_object('value', ay.p25, 'label', ay.p25_label),
                                                                      jsonb_build_object('value', ay.p75, 'label', ay.p75_label),
                                                                      jsonb_build_object('value', ay.max, 'label', ay.max_label))))
                   order by ord) as overlay
      from
          %s     ax,
          %s     ay,
          bivariate_overlays o,
          %s bix1 left join bivariate_unit_localization bulx1 on bix1.unit_id = bulx1.unit_id,
          %s bix2 left join bivariate_unit_localization bulx2 on bix2.unit_id = bulx2.unit_id,
          %s biy1 left join bivariate_unit_localization buly1 on biy1.unit_id = buly1.unit_id,
          %s biy2 left join bivariate_unit_localization buly2 on biy2.unit_id = buly2.unit_id
      where
            bix1.param_id = o.x_numerator
        and bix2.param_id = o.x_denominator
        and biy1.param_id = o.y_numerator
        and biy2.param_id = o.y_denominator
        and ax.denominator = o.x_denominator
        and ax.numerator = o.x_numerator
        and ay.denominator = o.y_denominator
        and ay.numerator = o.y_numerator )                                                      ov,
    %s                                                                              x,
    %s                                                                              y,
    %s bix1 left join bivariate_unit_localization bulx1 on bix1.unit_id = bulx1.unit_id,
    %s bix2 left join bivariate_unit_localization bulx2 on bix2.unit_id = bulx2.unit_id,
    %s biy1 left join bivariate_unit_localization buly1 on biy1.unit_id = buly1.unit_id,
    %s biy2 left join bivariate_unit_localization buly2 on biy2.unit_id = buly2.unit_id
where
      x.numerator = 'count'
  and x.denominator = 'area_km2'
  and y.numerator = 'view_count'
  and y.denominator = 'area_km2'
  and x.numerator = bix1.param_id
  and x.denominator = bix2.param_id
  and y.numerator = biy1.param_id
  and y.denominator = biy2.param_id
