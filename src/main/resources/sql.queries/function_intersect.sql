with validated_input as (
        select map_to_geometry_obj(:polygon) geom
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
                       population,
                       populated_area_km2,
                       count,
                       building_count,
                       highway_length,
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
