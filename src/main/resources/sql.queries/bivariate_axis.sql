select ax.numerator, ax.denominator, ind1.param_label numerator_label,
   ind2.param_label denominator_label
   from %s ax
   join %s ind1 on ind1.param_id = ax.numerator
   join %s ind2 on ind2.param_id = ax.denominator