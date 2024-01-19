--liquibase formatted sql
--changeset insights-api:create_bivariate_overlays_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists bivariate_overlays_v2
(
    ord                 float,
    name                text,
    description         text,
    x_numerator_id      uuid not null, -- vertical axis on DN
    x_denominator_id    uuid not null, -- vertical axis on DN
    y_numerator_id      uuid not null, -- horizontal axis on DN
    y_denominator_id    uuid not null, -- horizontal axis on DN
    active              boolean default true,
    colors              jsonb not null,
    application         json,
    is_public           boolean default false
);

alter table bivariate_overlays_v2 
    drop constraint if exists bivariate_overlays_v2_unique_key,
    add constraint bivariate_overlays_v2_unique_key
        unique (x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);
