--liquibase formatted sql
--changeset insights-api:add_table_bivariate_overlays splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_overlays;

create table bivariate_overlays
(
    ord           float,
    name          text,
    description   text,
    x_numerator   text, -- vertical axis on DN
    x_denominator text, -- vertical axis on DN
    y_numerator   text, -- horizontal axis on DN
    y_denominator text, -- horizontal axis on DN
    active        boolean,
    colors        jsonb,
    application   json,
    is_public     boolean
);
