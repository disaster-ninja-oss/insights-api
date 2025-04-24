--liquibase formatted sql
--changeset insights-api:add_column_coverage_polygon splitStatements:false stripComments:false endDelimiter:; runOnChange:true

alter table bivariate_indicators_metadata
add column if not exists coverage_polygon geometry;
