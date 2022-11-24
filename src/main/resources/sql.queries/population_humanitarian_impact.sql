with resolution as (
        select calculate_area_resolution(ST_SetSRID(:geometry::geometry, 4326)) as resolution
    ),
         validated_input as (
            select map_to_geometry_obj(:geometry) geom
         ),
        subdivided_polygons as (
                  select ST_Subdivide(v.geom) geom
                  from validated_input v
              ),
         stat_in_area as (
             select s.*, sum(population) over (order by population desc) as sum_pop
             from (
                      select distinct h3, population, area_km2, sh.geom
                      from stat_h3 sh,
                           subdivided_polygons p
                      where zoom = (select resolution from resolution)
                        and population > 0
                        and ST_Intersects(
                              sh.geom,
                              p.geom
                          )
                  ) s
         ),
         total as (
             select sum(population) as population, round(sum(area_km2)::numeric, 2) as area from stat_in_area
         )
    select sum(s.population)                                as population,
           case
               when sum_pop <= t.population * 0.68 then '0-68'
               else '68-100'
               end                                          as percentage,
           case
               when sum_pop <= t.population * 0.68 then 'Kontur Urban Core'
               else 'Kontur Settled Periphery'
               end                                          as name,
           round(sum(area_km2)::numeric, 2)                 as areaKm2,
           ST_AsGeoJSON(ST_Transform(ST_Union(geom), 4326)) as geometry,
           t.population                                     as totalPopulation,
           t.area                                           as totalAreaKm2
    from stat_in_area s,
         total t
    group by t.population, t.area, 2, 3