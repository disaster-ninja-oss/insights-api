with resolution as (
    select calculate_area_resolution(ST_SetSRID(:polygon::geometry, 4326)) as resolution
),
                     validated_input as (
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
                                         population,
                                         area_km2
                                  from stat_h3 sh
                                  where ST_Intersects(sh.geom, p.geom)
                                    and sh.zoom = (select resolution from resolution)
                                    and sh.population > 0
                                  order by h3
                                  ) h
                     ),
                     stat_pop as (
                         select s.*, sum(population) over (order by population desc) as sum_pop
                         from stat_area s
                     ),
                     total as (
                         select sum(population)                  as population,
                                round(sum(area_km2)::numeric, 2) as area
                         from stat_pop
                     )
                select sum(s.population)                as urbanCorePopulation,
                       round(sum(area_km2)::numeric, 2) as urbanCoreAreaKm2,
                       t.area                           as totalPopulatedAreaKm2
                from stat_pop s,
                     total t
                where sum_pop <= t.population * 0.68
                group by t.population, t.area;