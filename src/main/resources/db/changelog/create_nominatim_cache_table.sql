--liquibase formatted sql
--changeset insights-api:create_nominatim_cache_table splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists nominatim_cache
(
    query_hash  uuid primary key, -- md5 hash of query in UUID format
    query       text,
    response    jsonb,
    created_at  timestamptz default now()::timestamptz
);
