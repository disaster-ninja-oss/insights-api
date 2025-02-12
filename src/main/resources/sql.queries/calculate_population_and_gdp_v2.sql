with validated_input
         as (select :geometry::geometry as geom),
     boxinput as (select st_envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select st_subdivide(v.geom) geom from validated_input v),
     hexes as materialized (
             select distinct sh.h3
             from boxinput bi
                      cross join subdivision sb
                      join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom) and sh.resolution = 8)),
     res as (select st.h3, indicator_uuid, indicator_value, param_id
                      from stat_h3_transposed st
                      join hexes using(h3)
                      join bivariate_indicators_metadata m on (
                        st.indicator_uuid = m.internal_id and
                        param_id in ('gdp', 'population', 'residential') and
                        state = 'READY' and
                        owner = 'disaster.ninja'
                    )
                ),
     distinct_h3 as (select h3,
                            st_transform(h3_cell_to_geometry(h3), 3857) geom,
                            coalesce(max(indicator_value) filter (where param_id = 'population'), 0)    population,
                            coalesce(max(indicator_value) filter (where param_id = 'gdp'), 0)           gdp,
                            coalesce(max(indicator_value) filter (where param_id = 'residential'), 0)   residential
                     from res
                     group by h3
                ),
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
