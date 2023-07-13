with validated_input
         as (select :geometry::geometry as geom),
     boxinput as (select st_envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select st_subdivide(v.geom) geom from validated_input v),
     res as (select st.h3, indicator_uuid, indicator_value
             from boxinput bi
                      cross join subdivision sb
                      join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom))
                      join stat_h3_transposed st on (sh.h3 = st.h3)
             where sh.resolution = 8 and indicator_uuid IN (select param_uuid from %s where param_id IN ('gdp','population', 'residential'))),
     distinct_h3 as (select a.h3                                          h3,
                            st_transform(h3_cell_to_geometry(a.h3), 3857) geom,
                            a.indicator_value                             population,
                            b.indicator_value                             gdp,
                            c.indicator_value                             residential
                     from res a,
                          res b,
                          res c,
                          %s bi_a,
                          %s bi_b,
                          %s bi_c
                     where a.h3 = b.h3
                       and a.h3 = c.h3
                       and a.indicator_uuid = bi_a.param_uuid
                       and b.indicator_uuid = bi_b.param_uuid
                       and c.indicator_uuid = bi_c.param_uuid
                       and bi_a.param_id = 'population'
                       and bi_b.param_id = 'gdp'
                       and bi_c.param_id = 'residential'
                       and a.indicator_value > 0),
     distinct_h3_with_area as materialized (select *
    from distinct_h3 sh
    cross join
    lateral (
    select case
    when ST_Intersects(sh.geom, bound)
    then ST_Area(ST_Intersection(sh.geom, v.geom)) / ST_Area(sh.geom)
    else 1::double precision
    end "area"
    from validated_input v,
    ST_Boundary(v.geom) "bound"
    ) h)
select sum(dh.population * dh.area) as population,
       sum(dh.population * dh.residential * dh.area) as urban,
       sum(dh.gdp * dh.area)        as gdp,
       'population'                 as type
from distinct_h3_with_area dh;