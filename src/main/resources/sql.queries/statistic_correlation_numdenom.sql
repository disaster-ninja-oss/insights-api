select x.numerator                             as x_num,
       x.denominator                           as x_den,
       x_den_indicator.param_label             as x_param_label,
       y.numerator                             as y_num,
       y.denominator                           as y_den,
       y_den_indicator.param_label             as y_param_label,
       1 - ((1 - x.quality) * (1 - y.quality)) as quality
from (%s x
         join %s x_den_indicator
              on (x.denominator = x_den_indicator.param_id)
         join %s x_num_indicator
              on (x.numerator = x_num_indicator.param_id)),
     (%s y
         join %s y_den_indicator
              on (y.denominator = y_den_indicator.param_id))
where (x.numerator != y.numerator)
  and x.quality > 0.5
  and y.quality > 0.5
  and x_den_indicator.is_base
  and y_den_indicator.is_base
  and not x_num_indicator.is_base