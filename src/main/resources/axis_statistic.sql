select
        jsonb_build_object('label', label, 'quotient', jsonb_build_array(numerator, denominator), 'quality',
                           quality,
                           'steps', jsonb_build_array(
                                   jsonb_build_object('value', min, 'label', min_label),
                                   jsonb_build_object('value', p25, 'label', p25_label),
                                   jsonb_build_object('value', p75, 'label', p75_label),
                                   jsonb_build_object('value', max, 'label', max_label)))
from bivariate_axis