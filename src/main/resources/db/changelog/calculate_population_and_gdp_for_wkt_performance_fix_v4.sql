--liquibase formatted sql
--changeset insights-api:calculate_population_and_gdp_for_wkt_performance_fix_v4 splitStatements:false stripComments:false endDelimiter:; runOnChange:true
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
with validated_input as (
    select ST_MakeValid(ST_Transform(ST_UnaryUnion(
                                             ST_WrapX(ST_WrapX(ST_Union(
                                                                       ST_MakeValid(d.geom)
                                                                   ), 180, -360), -180, 360)
                                         ), 3857)) "geom"
    from ST_Dump(ST_CollectionExtract(ST_SetSRID(ST_GeomFromText(
                                                         wkt
                                                     ), 4326), 3)) d
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