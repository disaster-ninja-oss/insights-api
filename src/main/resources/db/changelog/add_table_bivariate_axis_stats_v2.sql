--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_stats_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_axis_stats_v2;
create table bivariate_axis_stats_v2
(
    numerator      text,
    denominator    text,
    min            double precision,
    p25            double precision,
    p75            double precision,
    max            double precision,
    corr           double precision,
    covar_pop      double precision,
    covar_samp     double precision,
    regr_avgx      double precision,
    regr_avgy      double precision,
    regr_count     bigint,
    regr_intercept double precision,
    regr_r2        double precision,
    regr_slope     double precision,
    regr_sxx       double precision,
    regr_sxy       double precision,
    regr_syy       double precision,
    stddev         numeric,
    stddev_pop     numeric,
    stddev_samp    numeric,
    variance       numeric,
    var_pop        numeric,
    var_samp       numeric
);
