--liquibase formatted sql
--changeset insights-api:calculate_area_resolution_performance_fix splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists calculate_area_resolution(geometry);

create or replace function calculate_area_resolution(geometry geometry)
    returns integer
    language plpgsql
    stable
    strict
    parallel safe
    cost 10000
as
$$
declare
    area_limit bigint := 10000;
    resolution int    := 8;
    geom_area  bigint := ST_Area(ST_UnaryUnion(ST_MakeValid(geometry))::geography) / 1000000;
begin
    select least(
                   sum(populated_area_km2),
                   ST_Area(ST_UnaryUnion(ST_MakeValid(geometry))::geography) / 1000000
               )::numeric "geom_area"
    from (
             select distinct h3,
                             populated_area_km2
             from stat_h3 sh,
                  ST_Subdivide(ST_MakeValid(ST_Transform(
                          ST_WrapX(ST_WrapX(
                                           ST_UnaryUnion(ST_CollectionExtract(geometry, 3)),
                                           180, -360), -180, 360),
                          3857))) g_geom
             where ST_Intersects(sh.geom, g_geom)
               and zoom = 4
               and population > 0
         ) "pop_area_h3"
    into geom_area;
    while area_limit < geom_area and resolution > 1
        loop
            resolution = resolution - 1;
            area_limit = area_limit * 7;
        end loop;
    return resolution;
end;
$$;