with resolution as (select calculate_area_resolution(ST_SetSRID(:polygon::geometry, 4326)) as resolution),
     validated_input
         as (select (:transformed_polygon)::geometry as geom),
     boxinput as (select st_envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select st_subdivide(v.geom) geom from validated_input v),
     res as (select st.h3, st.indicator_value as population, h3_cell_area(st.h3, 'km^2') as area_km2
             from boxinput bi
                      cross join subdivision sb
                      join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom))
                      join stat_h3_transposed st on (sh.h3 = st.h3)
             where sh.resolution = (select resolution from resolution)
               and indicator_uuid = (select param_uuid from %s where param_id = 'population')
               and indicator_value > 0),
     stat_pop as (select s.*, sum(population) over (order by population desc) as sum_pop
                  from res s),
     total as (select sum(population)                  as population,
                      round(sum(area_km2)::numeric, 2) as area
               from stat_pop)
select sum(s.population)                as urbanCorePopulation,
       round(sum(area_km2)::numeric, 2) as urbanCoreAreaKm2,
       t.area                           as totalPopulatedAreaKm2
from stat_pop s,
     total t
where sum_pop <= t.population * 0.68
group by t.population, t.area;