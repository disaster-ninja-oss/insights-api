--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_overrides splitStatements:false stripComments:false endDelimiter:; runOnChange:true
create table if not exists bivariate_axis_overrides as
select numerator_uuid, denominator_uuid, label, min, p25, p75, max, min_label, p25_label, p75_label, max_label
from bivariate_axis_v2
limit 0;

alter table bivariate_axis_overrides
    alter column numerator_uuid set not null,
    alter column denominator_uuid set not null,
    drop constraint if exists ba_overrides_unique,
    add constraint ba_overrides_unique unique (numerator_uuid, denominator_uuid),
    drop constraint if exists fk_ba_overrides_numerator_uuid,
    add constraint fk_ba_overrides_numerator_uuid foreign key (numerator_uuid)
        references bivariate_indicators_metadata(param_uuid) on delete cascade,
    drop constraint if exists fk_ba_overrides_denominator_uuid,
    add constraint fk_ba_overrides_denominator_uuid foreign key (denominator_uuid)
        references bivariate_indicators_metadata(param_uuid) on delete cascade;
