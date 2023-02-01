with pairs as (select a.indicator_value as numerator_value, b.indicator_value as denominator_value
               from stat_h3_transposed a,
                    stat_h3_transposed b
               where a.indicator_uuid = :numerator_uuid::uuid
                 and b.indicator_uuid = :denominator_uuid::uuid
                 and a.h3 = b.h3),
     calculations as (select floor(min(p.numerator_value / p.denominator_value::double precision))                               as min,
                             percentile_disc(0.33)
                             within group (order by p.numerator_value / p.denominator_value::double precision)::double precision as p25,
                             percentile_disc(0.66)
                             within group (order by p.numerator_value / p.denominator_value::double precision)::double precision as p75,
                             ceil(max(p.numerator_value / p.denominator_value::double precision))                                as max
                      from pairs p
                      where p.numerator_value != 0
                        and p.denominator_value != 0
                        and p.denominator_value > 0)
update %s ba
set min = c.min,
    p25 = c.p25,
    p75 = c.p75,
    max = c.max
from calculations c
where ba.numerator_uuid = :numerator_uuid::uuid
  and ba.denominator_uuid = :denominator_uuid::uuid