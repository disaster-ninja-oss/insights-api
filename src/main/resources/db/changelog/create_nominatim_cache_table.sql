--liquibase formatted sql
--changeset insights-api:create_nominatim_cache_table splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists nominatim_cache
(
    query       varchar(512) primary key,
    response    jsonb,
    created_at  timestamptz default now()::timestamptz
);
