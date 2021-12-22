--liquibase formatted sql
--changeset insights-api:calculate_population_and_gdp_for_wkt splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists calculate_population_and_gdp(text);

create or replace function calculate_population_and_gdp(geometry_string text)
    returns table
            (
                population double precision,
                urban      double precision,
                gdp        double precision,
                type       text
            )
    language sql
    stable
    strict
    parallel safe
    cost 10000
as
$$
with validated_input as (
    select calculate_validated_input(geometry_string) geom
),
     distinct_h3 as (
         select distinct on (h3) population,
                                 residential,
                                 gdp,
                                 area
         from stat_h3 sh
                  cross join
              lateral (
                  select case
                             when ST_Intersects(sh.geom, ST_ClipByBox2d(bound, sh.geom))
                                 then
                                     ST_Area(
                                             ST_Intersection(sh.geom, ST_ClipByBox2D(v.geom, sh.geom))
                                         ) / ST_Area(sh.geom)
                             else 1::double precision
                             end "area"
                  from validated_input v,
                       ST_Boundary(v.geom) "bound",
                       ST_Subdivide(v.geom, 30) sub_geom
                  where ST_Intersects(sh.geom, sub_geom)
                  ) h
         where sh.zoom = 8
           and sh.population > 0
     )
select sum(dh.population * dh.area)                  as population,
       sum(dh.population * dh.residential * dh.area) as urban,
       sum(dh.gdp * dh.area)                         as gdp,
       'population'                                  as type
from distinct_h3 dh;
$$;