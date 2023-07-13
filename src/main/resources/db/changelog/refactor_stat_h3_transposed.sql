--liquibase formatted sql
--changeset insights-api:refactor_stat_h3_transposed splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists stat_h3_transposed;

create table stat_h3_transposed
(
    h3              h3index not null,
    indicator_uuid  uuid    not null,
    indicator_value double precision not null
);

create index stat_h3_transposed_indicator_uuid_h3_idx
    on stat_h3_transposed (indicator_uuid, h3);



drop table if exists bivariate_indicators_wrk;

create table bivariate_indicators_metadata
(
    param_id         text                  not null,
    param_label      text,
    copyrights       json,
    direction        json,
    is_base          boolean default false not null,
    param_uuid       uuid                  not null
        constraint param_uuid_unique_constraint
            unique,
    owner            text                  not null,
    state            text,
    is_public        boolean,
    allowed_users    json,
    date             timestamp with time zone,
    description      text,
    coverage         text,
    update_frequency text,
    application      json,
    unit_id          text,
    last_updated     timestamp with time zone,
    constraint bivariate_indicators_metadata_pk
        primary key (param_id, owner)
);



drop table if exists stat_h3_geom;

create table stat_h3_geom
(
    h3         h3index,
    resolution integer,
    geom       geometry
);

create index stat_h3_geom_geom_resolution_idx
    on stat_h3_geom using gist (geom, resolution);

create index stat_h3_geom_h3_idx
    on stat_h3_geom (h3);
