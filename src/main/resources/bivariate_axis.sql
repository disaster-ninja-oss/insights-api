select ax.numerator, ax.denominator, ind1.param_label numerator_label,
   ind2.param_label denominator_label
   from bivariate_axis ax
   join bivariate_indicators ind1 on ind1.param_id = ax.numerator
   join bivariate_indicators ind2 on ind2.param_id = ax.denominator