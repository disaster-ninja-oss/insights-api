--liquibase formatted sql
--changeset insights-api:calculate_validated_input splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists calculate_validated_input(text);

create or replace function calculate_validated_input(geometry_string text)
    returns geometry
    language sql
    stable
    strict
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