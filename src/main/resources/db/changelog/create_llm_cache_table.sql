--liquibase formatted sql
--changeset insights-api:create_llm_cache_table splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists llm_cache
(
    hash        uuid primary key, -- md5 hash of request in UUID format
    request     text,
    response    text,
    model_name  text,
    created_at  timestamptz default now()::timestamptz
);
