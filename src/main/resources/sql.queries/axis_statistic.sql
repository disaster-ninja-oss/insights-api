select
    jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator),
                       'quotients', jsonb_build_array(
                               jsonb_build_object('name', bi1.param_id,
                                                  'label', bi1.param_label,
                                                  'direction', bi1.direction,
                                                  'description', bi1.description,
                                                  'coverage', bi1.coverage,
                                                  'update_frequency', bi1.update_frequency,
                                                  'unit', jsonb_build_object('id', bul1.unit_id,
                                                                             'shortName', bul1.short_name,
                                                                             'longName', bul1.long_name)),
                               jsonb_build_object('name', bi2.param_id,
                                                  'label', bi2.param_label,
                                                  'direction', bi2.direction,
                                                  'description', bi2.description,
                                                  'coverage', bi2.coverage,
                                                  'update_frequency', bi2.update_frequency,
                                                  'unit', jsonb_build_object('id', bul2.unit_id,
                                                                             'shortName', bul2.short_name,
                                                                             'longName', bul2.long_name))
                           ),
                       'quality', quality,
                       'steps', jsonb_build_array(
                               jsonb_build_object('value', min, 'label', min_label),
                               jsonb_build_object('value', p25, 'label', p25_label),
                               jsonb_build_object('value', p75, 'label', p75_label),
                               jsonb_build_object('value', max, 'label', max_label))) as axis
from
    %s,
    %s bi1 left join bivariate_unit_localization bul1 on bi1.unit_id = bul1.unit_id,
    %s bi2 left join bivariate_unit_localization bul2 on bi2.unit_id = bul2.unit_id
where
    numerator = bi1.param_id
  and denominator = bi2.param_id