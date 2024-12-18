--liquibase formatted sql
--changeset insights-api:add_table_bivariate_unit_localization splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_unit_localization;
create table bivariate_unit_localization
(
    unit_id   text not null primary key,
    language text,
    short_name text,
    long_name text
);

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('m', 'en', 'm', 'meters');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('km', 'en', 'km', 'kilometers');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('km2', 'en', 'km²', 'square kilometers');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('h', 'en', 'h', 'hours');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('days', 'en', 'days', 'days');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('deg', 'en', '°', 'degrees');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('nW_cm2_sr', 'en', 'nW/cm²/sr', 'watts per square centimeter per steradian');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('unixtime', 'en', 'date', 'date');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('celc_deg', 'en', '°C', 'degrees Celsius');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('W_m2', 'en', 'W/m²', 'watt per square metre');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('USD', 'en', 'USD', 'United States dollar');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('index', 'en', 'index', 'index');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('other', 'en', NULL, NULL);

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n', 'en', 'n', 'number');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('ppl', 'en', 'ppl', 'people');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('ppl*km', 'en', 'ppl*km', 'man-distance');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('perc', 'en', '%', 'percentage');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('fract', 'en', 'fraction', 'fraction');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('m_s2', 'en', 'm/s²', 'meters per square second');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_1', 'en', 'n per 1', 'number per one feature');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_10', 'en', 'n per 10', 'number per ten features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_100', 'en', 'n per 100', 'number per one hundred features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_1k', 'en', 'n per 1000', 'number per one thousand features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_10k', 'en', 'n per 10 thousands', 'number per ten thousands features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_100k', 'en', 'n per 100 thousands', 'number per one hundred thousands features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_1m', 'en', 'n per 1 million', 'number per million features');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('km_per_sq_km', 'en', 'km/km²', 'kilometers per square kilometer');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_10k_sq_km', 'en', 'n/10000km²', 'number per ten thousands square kilometers');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('n_per_ha', 'en', 'n/ha', 'number per hectare');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('years', 'en', 'years', 'years');

insert into bivariate_unit_localization (unit_id, language, short_name, long_name)
values ('billions_USD', 'en', 'B USD', 'billions of the United States dollar');
