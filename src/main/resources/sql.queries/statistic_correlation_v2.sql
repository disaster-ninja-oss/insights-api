select
    jsonb_build_object(
                      'x', jsonb_build_object('label', xcopy.param_label,
                                              'quotient',
                                              jsonb_build_array(x_numerator_id, x_denominator_id)),
                      'y', jsonb_build_object('label', ycopy.param_label,
                                              'quotient',
                                              jsonb_build_array(y_numerator_id, y_denominator_id)),
                      'rate', correlation,
                      'correlation', correlation,
                      'quality', quality,
                      'avgCorrelationX', avg(abs(correlation)) over (partition by x_numerator_id, x_denominator_id),
                      'avgCorrelationY', avg(abs(correlation)) over (partition by y_numerator_id, y_denominator_id)
                  ),
    avg(abs(correlation)) over (partition by x_numerator_id, x_denominator_id) * avg(abs(correlation)) over (partition by y_numerator_id, y_denominator_id) mult
from
    bivariate_axis_correlation_v2, bivariate_indicators_metadata xcopy, bivariate_indicators_metadata ycopy
    where xcopy.internal_id = x_numerator_id and ycopy.internal_id = y_numerator_id and correlation is not null
