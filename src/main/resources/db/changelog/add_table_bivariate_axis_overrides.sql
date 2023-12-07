--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_overrides splitStatements:false stripComments:false endDelimiter:; runOnChange:true
create table if not exists bivariate_axis_overrides as
select numerator, denominator, label, min, max, p25, p75, ''::text as owner
from bivariate_axis_v2
limit 0;

alter table bivariate_axis_overrides
add constraint ba_overrides_key unique (numerator, denominator, owner);
