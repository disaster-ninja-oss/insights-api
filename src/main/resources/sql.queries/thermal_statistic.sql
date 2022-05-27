with validated_input as (
    select calculate_validated_input(:polygon) geom
),
     stat_area as (
         select distinct on (h.h3) h.*
         from (
                  select ST_Subdivide(v.geom, 30) geom
                  from validated_input v
              ) p
                  cross join
              lateral (
                  select h3,
                         industrial_area,
                         wildfires,
                         volcanos_count,
                         forest
                  from stat_h3 sh
                  where ST_Intersects(sh.geom, p.geom)
                    and sh.zoom = 8
                  order by h3
                  ) h
     )
select %s from stat_area st