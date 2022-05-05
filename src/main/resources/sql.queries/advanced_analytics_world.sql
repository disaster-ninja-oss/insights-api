select ax.numerator, ax.denominator, ind1.param_label numerator_label,
   ind2.param_label denominator_label,
   ax.sum_value, ax.sum_quality, ax.min_value, ax.min_quality,
   ax.max_value, ax.max_quality, ax.stddev_value, ax.stddev_quality,
   ax.median_value, ax.median_quality, ax.mean_value, ax.mean_quality
   from bivariate_axis ax
   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
   join bivariate_indicators ind2 on ind2.param_id = ax.denominator