--liquibase formatted sql
--changeset insights-api:add_table_bivariate_unit splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_unit;
create table bivariate_unit
(
    id   text not null primary key,
    type text,
    measurement text,
    is_base boolean
);

insert into bivariate_unit (id, type, measurement, is_base)
values ('m', 'metric', 'length', TRUE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('km', 'metric', 'length', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('km2', 'metric', 'area', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('h', 'generic', 'time', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('days', 'generic', 'time', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('deg', 'generic', 'plane_angle', TRUE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('nW_cm2_sr', 'generic', 'radiance', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('unixtime', 'generic', 'unix_timestamp', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('celc_deg', 'generic', 'temperature', TRUE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('W_m2', 'generic', 'irradiance', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('USD', 'generic', 'money', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('index', 'generic', NULL, NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('other', 'generic', NULL, NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n', 'generic', NULL, NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('ppl', 'generic', 'people', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('ppl_km2', 'generic', 'man_distance', NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('perc', 'generic', NULL, NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('fract', 'generic', NULL, NULL);

insert into bivariate_unit (id, type, measurement, is_base)
values ('m_s2', 'metric', 'acceleration', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_1', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_10', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_100', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_1k', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_10k', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_100k', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_1m', 'generic', NULL, FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('km_per_sq_km', 'metric', 'density', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_10k_sq_km', 'generic', 'density', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('n_per_ha', 'generic', 'density', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('years', 'generic', 'time', FALSE);

insert into bivariate_unit (id, type, measurement, is_base)
values ('billions_USD', 'generic', 'money', NULL);
