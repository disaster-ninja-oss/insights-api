with validated_input as (
    select calculate_validated_input(:polygon) geom
),
     stat_area as (
                 select %s
                 from (
                          select ST_Subdivide(v.geom, 30) geom
                          from validated_input v
                      ) p
                          cross join
                      lateral (
                          select h3, %s
                          from stat_h3 sh
                          where ST_Intersects(sh.geom, p.geom)
                          ) h
     )
select * from stat_area