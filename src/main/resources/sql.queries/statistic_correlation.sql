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
    bivariate_axis_correlation_v2, bivariate_indicators_metadata xcopy, bivariate_indicators_metadata ycopy
    where xcopy.param_id = x_num and ycopy.param_id = y_num and correlation is not null
    order by mult desc
