--liquibase formatted sql
--changeset insights-api:add_mean_stddev_all_res_to_bivariate_axis_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true

alter table bivariate_axis_v2
    add column if not exists mean_all_res     double precision,
    add column if not exists stddev_all_res   double precision;
