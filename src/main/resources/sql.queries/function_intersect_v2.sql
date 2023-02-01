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
             where sh.zoom = 8
               and indicator_uuid IN (select param_uuid
                                      from %s
                                      where param_id IN ('population', 'populated_area_km2', 'count', 'building_count',
                                                         'highway_length', 'industrial_area', 'wildfires', 'volcanos_count', 'forest'))),
     stat_area as (select distinct on (h.h3) h.*
from (select a.h3,
    a.indicator_value as population,
    b.indicator_value as populated_area_km2,
    c.indicator_value as count,
    d.indicator_value as building_count,
    e.indicator_value as highway_length,
    f.indicator_value as industrial_area,
    g.indicator_value as wildfires,
    i.indicator_value as volcanos_count,
    j.indicator_value as forest
    from res a, res b, res c, res d, res e, res f, res g, res i, res j,
    %s bi_a,
    %s bi_b,
    %s bi_c,
    %s bi_d,
    %s bi_e,
    %s bi_f,
    %s bi_g,
    %s bi_i,
    %s bi_j
    where
    a.h3 = b.h3 and a.h3= c.h3 and a.h3=d.h3 and a.h3=e.h3 and a.h3=f.h3 and a.h3=g.h3 and a.h3=i.h3 and a.h3=j.h3
    and a.indicator_uuid = bi_a.param_uuid
    and b.indicator_uuid = bi_b.param_uuid
    and c.indicator_uuid = bi_c.param_uuid
    and d.indicator_uuid = bi_d.param_uuid
    and e.indicator_uuid = bi_e.param_uuid
    and f.indicator_uuid = bi_f.param_uuid
    and g.indicator_uuid = bi_g.param_uuid
    and i.indicator_uuid = bi_i.param_uuid
    and j.indicator_uuid = bi_j.param_uuid
    and bi_a.param_id = 'population'
    and bi_b.param_id = 'populated_area_km2'
    and bi_c.param_id = 'count'
    and bi_d.param_id = 'building_count'
    and bi_e.param_id = 'highway_length'
    and bi_f.param_id = 'industrial_area'
    and bi_g.param_id = 'wildfires'
    and bi_i.param_id = 'volcanos_count'
    and bi_j.param_id = 'forest'
    order by h3) h)
select %s
from stat_area st;