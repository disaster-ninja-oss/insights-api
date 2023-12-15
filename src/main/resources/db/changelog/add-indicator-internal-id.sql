--liquibase formatted sql
--changeset insights-api:add-indicator-internal-id.sql splitStatements:false stripComments:false endDelimiter:; runOnChange:true

-- Drop foreign keys on bivariate_axis_correlation_v2, bivariate_axis_v2 and bivariate_axis_overrides that use param_uuid

alter table bivariate_axis_correlation_v2
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_den_uuid,
    drop constraint if exists fk_bivariate_axis_correlation_v2_x_num_uuid,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_den_uuid,
    drop constraint if exists fk_bivariate_axis_correlation_v2_y_num_uuid;

alter table bivariate_axis_v2
    drop constraint if exists fk_bivariate_axis_v2_denominator_uuid,
    drop constraint if exists fk_bivariate_axis_v2_numerator_uuid;

alter table bivariate_axis_overrides
    drop constraint if exists fk_bivariate_axis_overrides_denominator_id,
    drop constraint if exists fk_bivariate_axis_overrides_numerator_id;

-- Drop param_uuid unique constraint on bivariate_indicators_metadata
alter table bivariate_indicators_metadata
    drop constraint param_uuid_unique_constraint;

-- Rename param_uuid column to internal_id
alter table bivariate_indicators_metadata
    rename column param_uuid to internal_id;

-- Add primary key for bivariate_indicators_metadata with internal_id
alter table bivariate_indicators_metadata
    add constraint bivariate_indicators_metadata_pk primary key (internal_id);

-- Add foreign keys on bivariate_axis_correlation_v2, bivariate_axis_v2 and bivariate_axis_overrides using internal_id
alter table bivariate_axis_correlation_v2
    add constraint fk_bivariate_axis_correlation_v2_x_den_uuid foreign key (x_den_uuid) references bivariate_indicators_metadata(internal_id),
    add constraint fk_bivariate_axis_correlation_v2_x_num_uuid foreign key (x_num_uuid) references bivariate_indicators_metadata(internal_id),
    add constraint fk_bivariate_axis_correlation_v2_y_den_uuid foreign key (y_den_uuid) references bivariate_indicators_metadata(internal_id),
    add constraint fk_bivariate_axis_correlation_v2_y_num_uuid foreign key (y_num_uuid) references bivariate_indicators_metadata(internal_id);

alter table bivariate_axis_v2
    add constraint fk_bivariate_axis_v2_denominator_uuid foreign key (denominator_uuid) references bivariate_indicators_metadata(internal_id),
    add constraint fk_bivariate_axis_v2_numerator_uuid foreign key (numerator_uuid) references bivariate_indicators_metadata(internal_id);

alter table bivariate_axis_overrides
    add constraint fk_bivariate_axis_overrides_denominator_id foreign key (denominator_id) references bivariate_indicators_metadata(internal_id),
    add constraint fk_bivariate_axis_overrides_numerator_id foreign key (numerator_id) references bivariate_indicators_metadata(internal_id);
