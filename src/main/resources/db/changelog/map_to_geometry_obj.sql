--liquibase formatted sql
--changeset insights-api:map_to_geometry_obj splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists calculate_validated_input(text);

drop function if exists map_to_geometry_obj(text);

--This function fixes an input geometry as much as possible. It makes geometry automatically valid, fixes invalid multipolygons,
--fixes problem with 180 meridian (if geometry crosses 180 meridian, bbox for it is drawn backwards and takes up half the world)
--Input: string with geometry collection as geojson or wkt in EPSG:4326
--Input example: {"type":"GeometryCollection","geometries":[{"type":"Point","coordinates":[-95.575,30.205]},
-- {"type":"Polygon","coordinates":[[[-95.58,30.2],[-95.58,30.21],[-95.57,30.21],[-95.57,30.2],[-95.58,30.2]]]}]}
--Output: fixed geometry in EPSG:3857
create or replace function map_to_geometry_obj(geometry_string text)
    returns geometry
    language sql
    stable
    parallel safe
    cost 10000
as
$$
    select ST_MakeValid(
        ST_Transform(
            ST_UnaryUnion(
                ST_WrapX(ST_WrapX(
                    ST_Union(
                        ST_MakeValid(d.geom)),
                    180, -360), -180, 360)),
            3857))
    from ST_Dump(ST_CollectionExtract(ST_SetSRID(geometry_string::geometry, 4326))) d;
$$;