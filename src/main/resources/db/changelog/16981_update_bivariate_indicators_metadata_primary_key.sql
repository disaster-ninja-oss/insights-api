--liquibase formatted sql
--changeset insights-api:16981_update_bivariate_indicators_metadata_primary_key.sql endDelimiter:; runOnChange:true

alter table if exists bivariate_indicators_metadata
    drop constraint bivariate_indicators_metadata_pk,
    drop constraint param_uuid_unique_constraint;


alter table if exists bivariate_indicators_metadata
    add constraint bivariate_indicators_metadata_pk primary key (param_uuid);