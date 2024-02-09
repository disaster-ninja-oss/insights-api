--liquibase formatted sql
--changeset insights-api:add_constraints_bivariate_axis_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true

alter table bivariate_axis_v2
    drop constraint if exists bivariate_axis_v2_unique,
    add constraint bivariate_axis_v2_unique unique (numerator_uuid, denominator_uuid),
    drop constraint if exists fk_bivariate_axis_v2_denominator_uuid,
    add constraint fk_bivariate_axis_v2_denominator_uuid foreign key (denominator_uuid) references bivariate_indicators_metadata(internal_id) on delete cascade,
    drop constraint if exists fk_bivariate_axis_v2_numerator_uuid,
    add constraint fk_bivariate_axis_v2_numerator_uuid foreign key (numerator_uuid) references bivariate_indicators_metadata(internal_id) on delete cascade;
