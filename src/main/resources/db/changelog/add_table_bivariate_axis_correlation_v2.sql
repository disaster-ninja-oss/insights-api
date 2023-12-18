--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_correlation_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists correlate_bivariate_axes_v2(text, text, text, text, text
    );
create or replace function correlate_bivariate_axes_v2
(
    table_name text, x_num text, x_den text, y_num text, y_den text
)
    returns float
as
$$
declare
select_query float;
begin
execute 'select corr(' || x_num || '/' || x_den || ',' || y_num || '/' || y_den || ') ' ||
        'from ' || table_name || ' ' ||
    -- denominators must be non-zero
        'where ' || x_den || '!= 0 and ' || y_den || ' != 0 ' ||
    -- one of numerators should be non-zero
        'and (' || x_num || '!= 0 or ' || y_num || '!=0)' into select_query;
return select_query;
end;
$$
language plpgsql stable parallel safe;


drop table if exists bivariate_axis_correlation_v2;
create table bivariate_axis_correlation_v2 (
    x_num text,
    x_den text,
    y_num text,
    y_den text,
    correlation double precision,
    quality double precision,
    x_num_uuid uuid,
    x_den_uuid uuid,
    y_num_uuid uuid,
    y_den_uuid uuid
);

alter table bivariate_axis_correlation_v2
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_num_uuid,
    add constraint fk_bivariate_axis_correlation_v2_x_num_uuid foreign key (x_num_uuid)
        references bivariate_indicators_metadata (param_uuid) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_den_uuid,
    add constraint fk_bivariate_axis_correlation_v2_x_den_uuid foreign key (x_den_uuid)
        references bivariate_indicators_metadata (param_uuid) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_num_uuid,
    add constraint fk_bivariate_axis_correlation_v2_y_num_uuid foreign key (y_num_uuid)
        references bivariate_indicators_metadata (param_uuid) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_den_uuid,
    add constraint fk_bivariate_axis_correlation_v2_y_den_uuid foreign key (y_den_uuid)
        references bivariate_indicators_metadata (param_uuid) on delete cascade,
    drop constraint if exists bivariate_axis_correlation_v2_unique_key,
    add constraint bivariate_axis_correlation_v2_unique_key
        unique (x_num_uuid, x_den_uuid, y_num_uuid, y_den_uuid);
