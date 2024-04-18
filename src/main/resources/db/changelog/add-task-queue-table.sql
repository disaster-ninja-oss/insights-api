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
        if (select version() ~ 'PostgreSQL 16') then
            -- 'nulls not distinct' is syntax error for pg14, so wrapping it in `execute`
            execute 'alter table task_queue
                add constraint task_queue_unique unique nulls not distinct (
                    task_type, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id)';
        else
            create unique index task_queue_1_unique on task_queue (task_type, x_numerator_id)
                where x_denominator_id is null and y_numerator_id is null and y_denominator_id is null;
            create unique index task_queue_2_unique on task_queue (task_type, x_numerator_id, x_denominator_id)
                where x_denominator_id is not null and y_numerator_id is null and y_denominator_id is null;
            create unique index task_queue_4_unique on task_queue (task_type, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id)
                where x_denominator_id is not null and y_numerator_id is not null and y_denominator_id is not null;
        end if;
    else
        raise notice 'skip task_queue_unique creation: already exists';
    end if;
end;
$$
