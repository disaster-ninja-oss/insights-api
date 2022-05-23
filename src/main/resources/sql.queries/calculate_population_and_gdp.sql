with validated_input as materialized (
    select calculate_validated_input(:geometry) as geom
),
     distinct_h3 as (
         select distinct on (h3) population,
                                 residential,
                                 gdp,
                                 sh.geom
         from stat_h3 sh,
              validated_input v,
              ST_Subdivide(v.geom, 30) sub_geom
         where sh.zoom = 8
           and sh.population > 0
           and ST_Intersects(sh.geom, sub_geom)
     ),
     distinct_h3_with_area as materialized (
         select * from
             distinct_h3 sh
                 cross join
             lateral (
                 select
                     case
                         when ST_Intersects(sh.geom, bound)
                             then ST_Area(ST_Intersection(sh.geom, v.geom)) / ST_Area(sh.geom)
                         else 1::double precision
                         end "area"
                 from validated_input v,
                      ST_Boundary(v.geom) "bound"
                 ) h
     )
select sum(dh.population * dh.area)                  as population,
       sum(dh.population * dh.residential * dh.area) as urban,
       sum(dh.gdp * dh.area)                         as gdp,
       'population'                                  as type
from distinct_h3_with_area dh;