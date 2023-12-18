--liquibase formatted sql
--changeset insights-api:rebuild_bivariate_axis_correlation_v2 splitStatements:false stripComments:false endDelimiter:; runOnChange:true

drop function if exists correlate_bivariate_axes_v2(text, text, text, text, text); -- moved to insights-db

drop table if exists bivariate_axis_correlation_v2;
create table bivariate_axis_correlation_v2 (
    correlation double precision,
    quality double precision,
    x_numerator_id uuid,
    x_denominator_id uuid,
    y_numerator_id uuid,
    y_denominator_id uuid
);

alter table bivariate_axis_correlation_v2
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_numerator_id,
    add constraint fk_bivariate_axis_correlation_v2_x_numerator_id foreign key (x_numerator_id)
        references bivariate_indicators_metadata (internal_id) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_denominator_id,
    add constraint fk_bivariate_axis_correlation_v2_x_denominator_id foreign key (x_denominator_id)
        references bivariate_indicators_metadata (internal_id) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_numerator_id,
    add constraint fk_bivariate_axis_correlation_v2_y_numerator_id foreign key (y_numerator_id)
        references bivariate_indicators_metadata (internal_id) on delete cascade,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_denominator_id,
    add constraint fk_bivariate_axis_correlation_v2_y_denominator_id foreign key (y_denominator_id)
        references bivariate_indicators_metadata (internal_id) on delete cascade,
    drop constraint if exists bivariate_axis_correlation_v2_unique_key,
    add constraint bivariate_axis_correlation_v2_unique_key
        unique (x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);
