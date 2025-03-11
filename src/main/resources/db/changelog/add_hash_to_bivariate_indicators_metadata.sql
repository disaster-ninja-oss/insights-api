--liquibase formatted sql
--changeset insights-api:add_hash_to_bivariate_indicators_metadata.sql endDelimiter:; runOnChange:true

alter table bivariate_indicators_metadata
    add column if not exists hash text;
