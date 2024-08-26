--liquibase formatted sql
--changeset insights-api:create_search_history_table splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table search_history (
    app_id  uuid,
    query   text,
    search_results      jsonb,
    selected_feature    jsonb,
    selected_feature_type   text,
    created_at  timestamptz default now()::timestamptz
);
