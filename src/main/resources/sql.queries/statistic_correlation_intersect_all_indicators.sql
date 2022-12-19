with validated_input as (
    select (:polygon)::geometry as geom
), boxinput as (select st_envelope(v.geom) as bbox
                from validated_input as v
), subdivision as (
    select ST_Subdivide(v.geom) geom
    from validated_input v),
     existing_indicators as (
         select distinct indicator_uuid
         from boxinput as bi
                  cross join subdivision as sb
                  join stat_h3_geom sh on (sh.geom && bi.bbox and ST_Intersects(sh.geom, sb.geom))
                  join stat_h3_transposed st on (sh.h3 = st.h3)
         where indicator_value != 0),
    res as (select st.h3, indicator_uuid, indicator_value
from boxinput bi
    cross join subdivision sb
    join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom))
    join stat_h3_transposed st on (sh.h3 = st.h3)
order by st.h3, indicator_uuid),
    normalized_indicators as (
select a.indicator_uuid as numerator_uuid,
    b.indicator_uuid as denominator_uuid,
    (a.indicator_value / b.indicator_value) as normalized_value,
    a.h3
from res a, res b, bivariate_indicators_wrk as bi_b, existing_indicators as ex_a, existing_indicators as ex_b
where b.indicator_value != 0 and bi_b.is_base and bi_b.param_uuid = b.indicator_uuid and a.h3 = b.h3 and a.indicator_uuid = ex_a.indicator_uuid and b.indicator_uuid = ex_b.indicator_uuid
order by a.h3
    )
select a.numerator_uuid as xNumUuid,
       a.denominator_uuid as xDenUuid,
       b.numerator_uuid as yNumUuid,
       b.denominator_uuid as yDenUuid,
       corr(a.normalized_value, b.normalized_value) as metrics
from normalized_indicators a,
     normalized_indicators b
where a.h3 = b.h3
group by 1, 2, 3, 4;