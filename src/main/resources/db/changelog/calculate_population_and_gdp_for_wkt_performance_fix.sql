--liquibase formatted sql
--changeset insights-api:calculate_population_and_gdp_for_wkt_performance_fix splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists calculate_population_and_gdp_for_wkt(text);

create or replace function calculate_population_and_gdp_for_wkt(wkt text)
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
with subdivided_polygons as (
    select ST_Subdivide(
                   ST_MakeValid(ST_Transform(
                           ST_WrapX(ST_WrapX(
                                            ST_UnaryUnion(
                                                    ST_CollectionExtract(ST_SetSRID(ST_GeomFromText(wkt), 4326), 3)
                                                ),
                                            180, -360), -180, 360),
                           3857))
               ) as geom
)
select sum(sh.population * a.area)                  as population,
       sum(sh.population * sh.residential * a.area) as urban,
       sum(sh.gdp * a.area)                         as gdp,
       'population'                                 as type
from stat_h3 sh,
     subdivided_polygons p,
     lateral (
         select case
                    when ST_Contains(p.geom, sh.geom) then 1.
                    else ST_Area(ST_Intersection(sh.geom, p.geom)) / ST_Area(sh.geom)
                    end "area"
         ) "a"
where ST_Intersects(sh.geom, p.geom)
  and sh.zoom = 8
  and sh.population > 0;
$$;