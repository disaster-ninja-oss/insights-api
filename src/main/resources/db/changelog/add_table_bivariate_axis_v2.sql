--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_axis_v2;
create table bivariate_axis_v2 as (
    select a.param_id             as numerator,
           b.param_id             as denominator,
           null::double precision as min,
           null::double precision as p25,
           null::double precision as p75,
           null::double precision as max,
           null::double precision as quality,
           ''::text               as min_label,
           ''::text               as p25_label,
           ''::text               as p75_label,
           ''::text               as max_label,
           ''::text               as label,
           null::double precision as sum_value,
           null::double precision as sum_quality,
           null::double precision as min_value,
           null::double precision as min_quality,
           null::double precision as max_value,
           null::double precision as max_quality,
           null::double precision as stddev_value,
           null::double precision as stddev_quality,
           null::double precision as median_value,
           null::double precision as median_quality,
           null::double precision as mean_value,
           null::double precision as mean_quality
    from bivariate_indicators as a,
        bivariate_indicators as b
    where b.is_base and a.param_id != b.param_id
);
