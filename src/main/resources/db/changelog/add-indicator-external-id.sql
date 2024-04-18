--liquibase formatted sql
--changeset insights-api:add-indicator-internal-id.sql splitStatements:false stripComments:false endDelimiter:; runOnChange:true

create extension if not exists "uuid-ossp";

-- Add external_id column

alter table bivariate_indicators_metadata
    add column if not exists external_id uuid;

update bivariate_indicators_metadata
    set external_id = new_external_id
from (
    select param_id unique_param_id, owner unique_owner, uuid_generate_v4() new_external_id
    from bivariate_indicators_metadata
    group by param_id, owner
) as t
where param_id = unique_param_id and owner = unique_owner;

alter table bivariate_indicators_metadata
    alter column external_id set not null;