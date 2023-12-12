--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_overrides splitStatements:false stripComments:false endDelimiter:; runOnChange:true
create table if not exists bivariate_axis_overrides (
    numerator_id uuid not null,
    denominator_id uuid not null,
    label text,
    min double precision,
    p25 double precision,
    p75 double precision,
    max double precision,
    min_label text,
    p25_label text,
    p75_label text,
    max_label text
);

alter table bivariate_axis_overrides
    drop constraint if exists bivariate_axis_overrides_inique_key,
    add constraint bivariate_axis_overrides_inique_key unique (numerator_id, denominator_id),
    drop constraint if exists fk_bivariate_axis_overrides_numerator_id,
    add constraint fk_bivariate_axis_overrides_numerator_id foreign key (numerator_id)
        references bivariate_indicators_metadata(param_uuid) on delete cascade,
    drop constraint if exists fk_bivariate_axis_overrides_denominator_id,
    add constraint fk_bivariate_axis_overrides_denominator_id foreign key (denominator_id)
        references bivariate_indicators_metadata(param_uuid) on delete cascade;
