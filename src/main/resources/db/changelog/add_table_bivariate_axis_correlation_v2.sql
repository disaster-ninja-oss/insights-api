--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_correlation_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop function if exists correlate_bivariate_axes(text, text, text, text, text
    );
create or replace function correlate_bivariate_axes
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
create table bivariate_axis_correlation_v2 as (
    select
        x.numerator as x_num,
        x.denominator as x_den,
        y.numerator as y_num,
        y.denominator as y_den,
        correlate_bivariate_axes('stat_h3_quality', x.numerator, x.denominator, y.numerator, y.denominator) as correlation,
        1 - ((1 - x.quality) * (1 - y.quality)) as quality
    from
        (bivariate_axis_v2 x
            join bivariate_indicators_metadata x_den_indicator
            on (x.denominator = x_den_indicator.param_id)
            join bivariate_indicators_metadata x_num_indicator
         on (x.numerator = x_num_indicator.param_id)),
        (bivariate_axis_v2 y
            join bivariate_indicators_metadata y_den_indicator
         on (y.denominator = y_den_indicator.param_id))
    where
        x.numerator != y.numerator
      and x.quality > 0.5
      and y.quality > 0.5
      and x_den_indicator.is_base
      and y_den_indicator.is_base
      and not x_num_indicator.is_base
);