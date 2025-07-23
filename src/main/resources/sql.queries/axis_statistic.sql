select
    jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator),
                       'transformation', default_transform,
                       'quotients', jsonb_build_array(
                               jsonb_build_object('name', bi1.param_id,
                                                  'label', bi1.param_label,
                                                  'emoji', bi1.emoji,
                                                  'direction', bi1.direction,
                                                  'description', bi1.description,
                                                  'copyrights', bi1.copyrights,
                                                  'coverage', bi1.coverage,
                                                  'update_frequency', bi1.update_frequency,
                                                  'layer_spatial_res', bi1.layer_spatial_res,
                                                  'layer_temporal_ext', bi1.layer_temporal_ext,
                                                  'category', bi1.category,
                                                  'unit', jsonb_build_object('id', bul1.unit_id,
                                                                             'shortName', bul1.short_name,
                                                                             'longName', bul1.long_name)),
                               jsonb_build_object('name', bi2.param_id,
                                                  'label', bi2.param_label,
                                                  'emoji', bi2.emoji,
                                                  'direction', bi2.direction,
                                                  'description', bi2.description,
                                                  'copyrights', bi2.copyrights,
                                                  'coverage', bi2.coverage,
                                                  'update_frequency', bi2.update_frequency,
                                                  'layer_spatial_res', bi2.layer_spatial_res,
                                                  'layer_temporal_ext', bi2.layer_temporal_ext,
                                                  'category', bi2.category,
                                                  'unit', jsonb_build_object('id', bul2.unit_id,
                                                                             'shortName', bul2.short_name,
                                                                             'longName', bul2.long_name))
                           ),
                       'quality', quality,
                       'steps', jsonb_build_array(
                               jsonb_build_object('value', floor(min), 'label', min_label),
                               jsonb_build_object('value', p25, 'label', p25_label),
                               jsonb_build_object('value', p75, 'label', p75_label),
                               jsonb_build_object('value', ceil(max), 'label', max_label))) as axis
from
    bivariate_axis_v2,
    bivariate_indicators_metadata bi1 left join bivariate_unit_localization bul1 on bi1.unit_id = bul1.unit_id,
    bivariate_indicators_metadata bi2 left join bivariate_unit_localization bul2 on bi2.unit_id = bul2.unit_id
where
    numerator_uuid = bi1.internal_id
and denominator_uuid = bi2.internal_id
and bi1.state='READY' and bi2.state='READY'
