--liquibase formatted sql
--changeset insights-api:add_columns_layer_fields splitStatements:false stripComments:false endDelimiter:; runOnChange:true

alter table bivariate_indicators_metadata
    add column if not exists layer_spatial_res text,
    add column if not exists layer_temporal_ext text,
    add column if not exists category json;
