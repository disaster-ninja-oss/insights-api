--liquibase formatted sql
--changeset insights-api:add_max_res_downscale_to_bivariate_indicators_metadata.sql endDelimiter:; runOnChange:true

alter table bivariate_indicators_metadata
    add column if not exists max_res int default 8,
    add column if not exists downscale text;

update bivariate_indicators_metadata m
set downscale = (
    select case when a.quality>b.quality then 'proportional' else 'equal' end
    from bivariate_axis_v2 a
    join bivariate_axis_v2 b on (
        a.numerator_uuid = m.internal_id and
        a.numerator_uuid = b.numerator_uuid and
        a.denominator_uuid = '00000000-0000-0000-0000-000000000000' and 
        b.denominator_uuid = '11111111-1111-1111-1111-111111111111'
    )
);
