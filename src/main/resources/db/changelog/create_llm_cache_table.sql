--liquibase formatted sql
--changeset insights-api:create_llm_cache_table splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists llm_cache
(
    hash        char(32) primary key, -- md5 hash of request
    request     text,
    response    text,
    created_at  timestamptz default now()::timestamptz
);
