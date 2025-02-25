select x.numerator                             as x_num,
       x.denominator                           as x_den,
       x_den_indicator.param_label             as x_param_label,
       x_den_indicator.internal_id              as x_den_internal_id,
       x_num_indicator.internal_id              as x_num_internal_id,
       y.numerator                             as y_num,
       y.denominator                           as y_den,
       y_den_indicator.param_label             as y_param_label,
       y_den_indicator.internal_id              as y_den_internal_id,
       y_num_indicator.internal_id              as y_num_internal_id,
       1 - ((1 - x.quality) * (1 - y.quality)) as quality
from (bivariate_axis_v2 x
         join bivariate_indicators_metadata x_den_indicator
              on (x.denominator = x_den_indicator.param_id)
         join bivariate_indicators_metadata x_num_indicator
              on (x.numerator = x_num_indicator.param_id)),
     (bivariate_axis_v2 y
         join bivariate_indicators_metadata y_den_indicator
              on (y.denominator = y_den_indicator.param_id)
         join bivariate_indicators_metadata y_num_indicator
              on (y.numerator = y_num_indicator.param_id))
where (x.numerator != y.numerator)
  and x.quality > 0.5
  and y.quality > 0.5
  and x_den_indicator.is_base
  and y_den_indicator.is_base
  and not x_num_indicator.is_base
