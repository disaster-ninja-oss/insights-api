with resolution as (select calculate_area_resolution_v2(ST_SetSRID(:geometry::geometry, 4326)) as resolution),
     validated_input
         as (select (:transformed_geometry)::geometry as geom),
     boxinput as (select st_envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select st_subdivide(v.geom) geom from validated_input v),
     hexes as materialized (
             select distinct sh.h3
             from boxinput bi
                      cross join subdivision sb
                      join stat_h3_geom sh on (sh.geom && bi.bbox and st_intersects(sh.geom, sb.geom) and sh.resolution = (select resolution from resolution))),
     res as (select st.h3, indicator_uuid, indicator_value
             from hexes
             JOIN stat_h3_transposed st USING(h3)
             WHERE
                 indicator_uuid in (%s)),
     indicators_as_columns as (
         SELECT
             h3,
             COALESCE(MAX(indicator_value) FILTER (WHERE indicator_uuid = '%s'), 0) AS population,
             COALESCE(MAX(indicator_value) FILTER (WHERE indicator_uuid = '%s'), 0) AS populated_area_km2,
             h3_cell_to_boundary_geometry(h3) as geom
         FROM res
         GROUP BY h3
     ),
     stat_in_area as (select s.*, sum(population) over (order by population desc) as sum_pop from indicators_as_columns s),
     total as (select sum(population) as population, round(sum(populated_area_km2)::numeric, 2) as area from stat_in_area)
select sum(s.population)                                as population,
       case
           when sum_pop <= t.population * 0.68 then '0-68'
           else '68-100'
           end                                          as percentage,
       case
           when sum_pop <= t.population * 0.68 then 'Kontur Urban Core'
           else 'Kontur Settled Periphery'
           end                                          as name,
       round(sum(populated_area_km2)::numeric, 2)       as areaKm2,
       ST_AsGeoJSON(ST_Transform(ST_Union(geom), 4326)) as geometry,
       t.population                                     as totalPopulation,
       t.area                                           as totalAreaKm2
from stat_in_area s,
     total t
group by t.population, t.area, 2, 3;
