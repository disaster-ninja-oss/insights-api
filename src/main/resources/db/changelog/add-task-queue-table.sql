--liquibase formatted sql
--changeset insights-api:add-task-queue-table.sql splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists task_queue
(
    task_type        text             not null,
    x_numerator_id   uuid,
    x_denominator_id uuid,
    y_numerator_id   uuid,
    y_denominator_id uuid,
    priority         double precision not null,
    created_at       timestamptz default now()::timestamptz
);

do $$
begin
    if not exists (select
                   from information_schema.constraint_table_usage
                   where table_name='task_queue' and constraint_name='task_queue_unique') then
        alter table task_queue
            add constraint task_queue_unique unique nulls not distinct (
                task_type, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);
    else
        raise notice 'skip task_queue_unique creation: already exists';
    end if;
end;
$$
