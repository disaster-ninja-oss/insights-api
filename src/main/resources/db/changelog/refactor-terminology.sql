--liquibase formatted sql
--changeset insights-api:refactor-terminology.sql splitStatements:false stripComments:false endDelimiter:; runOnChange:true

alter table bivariate_axis_v2
    drop constraint if exists fk_bivariate_axis_v2_denominator_uuid,
    drop constraint if exists fk_bivariate_axis_v2_numerator_uuid,
    drop column if exists numerator,
    drop column if exists denominator;

alter table bivariate_axis_v2
    rename column numerator_uuid to numerator_id;

alter table bivariate_axis_v2
    rename column denominator_uuid to denominator_id;

alter table bivariate_axis_v2
    add constraint fk_bivariate_axis_v2_denominator_id foreign key (denominator_id) references bivariate_indicators_metadata (internal_id),
    add constraint fk_bivariate_axis_v2_numerator_id foreign key (numerator_id) references bivariate_indicators_metadata (internal_id);

alter table bivariate_indicators_metadata
    rename column param_label to label;

alter table stat_h3_transposed
    rename column indicator_uuid to indicator_id;

create table bivariate_overlays_v2
(
    ord              double precision,
    name             text,
    description      text,
    x_numerator_id   uuid,
    x_denominator_id uuid,
    y_numerator_id   uuid,
    y_denominator_id uuid,
    active           boolean,
    colors           jsonb,
    application      json,
    is_public        boolean
);

