--liquibase formatted sql
--changeset insights-api:add-task-queue-table.sql splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create table if not exists task_queue
(
    task_type   text not null,
    x_numerator_id uuid,
    x_denominator_id uuid,
    y_numerator_id uuid,
    y_denominator_id uuid,
    priority   double precision       not null,
    created_at  timestamptz  default now()::timestamptz
);

alter table task_queue
    add constraint task_queue_pk primary key (task_type, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);

