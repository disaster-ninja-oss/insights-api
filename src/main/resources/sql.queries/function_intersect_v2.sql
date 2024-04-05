with validated_input as (
    select (:polygon)::geometry as geom
),
     boxinput as (select st_envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select st_subdivide(v.geom) geom from validated_input v),
     res as (select st.h3, indicator_uuid, indicator_value
             from boxinput bi
                      cross join subdivision sb
                      join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom))
                      join stat_h3_transposed st on (sh.h3 = st.h3)
             where sh.resolution = 8
               and indicator_uuid IN (select internal_id
                                      from %s
                                      where param_id IN ('%s') and state = 'READY' and is_public)),
     stat_area as (select distinct on (h.h3) h.*
from (select res_0.h3,
    %s
    from res %s,
    %s
    where %s%s
    and %s
    order by h3) h)
select %s
from stat_area st;
