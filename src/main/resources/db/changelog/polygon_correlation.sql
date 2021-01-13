--liquibase formatted sql
--changeset insights-api:polygon_correlation splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists correlate_bivariate_axes(json, text, text, text, text);

create function correlate_bivariate_axes(polygon json, x_num text, x_den text, y_num text, y_den text)
    returns double precision
as
$function$
declare
    result double precision;
begin
    execute 'select corr(' || x_num || '/' || x_den || ',' || y_num || '/' || y_den || ') ' ||
           'from stat_h3 where ' || x_den || '!= 0 and ' || y_den || ' != 0 ' ||
           'and ST_Intersects(geom, ST_Transform(ST_GeomFromGeoJSON(''' || polygon || '''), 3857))' into result;
    return result;
end;
$function$
    language plpgsql;