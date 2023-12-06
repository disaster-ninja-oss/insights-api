--liquibase formatted sql
--changeset insights-api:add_table_bivariate_axis_overrides splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_axis_overrides;

create table bivariate_axis_overrides as
select numerator, denominator, label, min, max, p25, p75
from bivariate_axis_v2
limit 0;

alter table bivariate_axis_overrides
add constraint ba_overrides_key unique (numerator, denominator);

insert into bivariate_axis_overrides
    (numerator, denominator, label)
values
    ('population', 'area_km2', 'Population (ppl/km²)'),
    ('count', 'area_km2', 'OSM objects (n/km²)'),
    ('building_count', 'area_km2', 'Buildings (n/km²)'),
    ('building_count', 'total_building_count', 'OSM Building completeness'),
    ('local_hours', 'area_km2', 'Edits by active locals (h/km²)'),
    ('total_hours', 'area_km2', 'Edits by all mappers (h/km²)'),
    ('view_count', 'area_km2', 'Map views, last 30 days (n/km²)'),
    ('osm_users', 'one', 'OpenStreetMap Contributors (n)'),
    ('total_building_count', 'area_km2', 'Total buildings count (n/km²)'),
    ('wildfires', 'area_km2', 'Wildfires (n/km²)'),
    ('hazardous_days_count', 'area_km2', 'Number of days with any disaster occurs, last year (n/km²)'),
    ('hazardous_days_count', 'one', 'Number of days with any disaster occurs, last year (n)'),
    ('earthquake_days_count', 'area_km2', 'Number of days under earthquake impact, last year (n/km²)'),
    ('earthquake_days_count', 'one', 'Number of days under earthquake impact, last year (n)'),
    ('drought_days_count', 'area_km2', 'Number of days under drought impact, last year (n/km²)'),
    ('drought_days_count', 'one', 'Number of days under drought impact, last year (n)'),
    ('cyclone_days_count', 'area_km2', 'Number of days under cyclone impact, last year (n/km²)'),
    ('cyclone_days_count', 'one', 'Number of days under cyclone impact, last year (n)'),
    ('wildfire_days_count', 'area_km2', 'Number of days under wildfire impact, last year (n/km²)'),
    ('wildfire_days_count', 'one', 'Number of days under wildfire impact, last year (n)'),
    ('volcano_days_count', 'area_km2', 'Number of days under volcano impact, last year (n/km²)'),
    ('volcano_days_count', 'one', 'Number of days under volcano impact, last year (n)'),
    ('flood_days_count', 'area_km2', 'Number of days under flood impact, last year (n/km²)'),
    ('flood_days_count', 'one', 'Number of days under flood impact, last year (n)'),
    ('forest', 'area_km2', 'Forest Landcover Area (km²/km²)'),
    ('days_maxtemp_over_32c_1c', 'one', 'Number of days per year'),
    ('days_mintemp_above_25c_1c', 'one', 'Number of nights per year'),
    ('man_distance_to_fire_brigade', 'one', 'Man-distance to fire brigade'),
    ('man_distance_to_hospital', 'one', 'Man-distance to hospitals'),
    ('man_distance_to_fire_brigade', 'population', 'Distance to fire station (km)'),
    ('highway_length', 'area_km2', 'OSM roads density (m/km²)'),
    ('total_road_length', 'area_km2', 'Total roads density estimate (km/km²)'),
    ('foursquare_places_count', 'one', 'Foursquare Japan places count'),
    ('foursquare_visits_count', 'one', 'Foursquare Japan visits count'),
    ('view_count_bf2402', 'one', 'Map views 30 days before 24.02.2022'),
    ('view_count_bf2402', 'area_km2', 'OSM Map Views, Jan 25-Feb 24 2022 (n/km²)'),
    ('powerlines', 'one', 'Medium voltage powerlines distribution estimation'),
    ('highway_length', 'total_road_length', 'OSM roads completeness'),
    ('night_lights_intensity', 'one', 'VIIRS Nighttime lights intensity'),
    ('eatery_count', 'one', 'Number of OSM eateries'),
    ('food_shops_count', 'one', 'Number of OSM food shops'),
    ('man_distance_to_bomb_shelters', 'one', 'Man-distance to bomb shelters'),
    ('man_distance_to_charging_stations', 'one', 'Man-distance to electric car charging stations'),
    ('man_distance_to_charging_stations', 'population', 'Distance to electric car charging stations (km)'),
    ('waste_basket_coverage_area_km2' , 'populated_area_km2', 'Waste basket coverage (coverage / populated area)'),
    ('man_distance_to_bomb_shelters', 'population', 'Distance to shelters (km)'),
    ('solar_power_plants', 'area_km2', 'Solar power plants');

insert into bivariate_axis_overrides
    (numerator, denominator, p25)
values
    ('waste_basket_coverage_area_km2' , 'populated_area_km2', 0.2),
    ('man_distance_to_bomb_shelters', 'population', 3.0),
    ('man_distance_to_charging_stations', 'population', 3.0),
    ('man_distance_to_fire_brigade', 'population', 3.0)
on conflict (numerator, denominator) do update
set p25 = excluded.p25;

insert into bivariate_axis_overrides
    (numerator, denominator, p75)
values
    ('waste_basket_coverage_area_km2' , 'populated_area_km2', 0.5),
    ('man_distance_to_bomb_shelters', 'population', 10.0),
    ('man_distance_to_charging_stations', 'population', 30.0),
    ('man_distance_to_fire_brigade', 'population', 10.0),
    ('building_count', 'total_building_count', 0.9),
    ('highway_length', 'total_road_length', 0.9)
on conflict (numerator, denominator) do update
set p75 = excluded.p75;

insert into bivariate_axis_overrides
    (numerator, denominator, max)
values
    ('waste_basket_coverage_area_km2' , 'populated_area_km2', 1.0),
    ('highway_length', 'total_road_length', 1.01)
on conflict (numerator, denominator) do update
set max = excluded.max;

insert into bivariate_axis_overrides
    (numerator, denominator, min)
values
    ('eatery_count', 'one', 0),
    ('food_shops_count', 'one', 0),
    ('hazardous_days_count', 'area_km2', 0),
    ('hazardous_days_count', 'one', 0),
    ('earthquake_days_count', 'area_km2', 0),
    ('earthquake_days_count', 'one', 0),
    ('drought_days_count', 'area_km2', 0),
    ('drought_days_count', 'one', 0),
    ('cyclone_days_count', 'area_km2', 0),
    ('cyclone_days_count', 'one', 0),
    ('wildfire_days_count', 'area_km2', 0),
    ('wildfire_days_count', 'one', 0),
    ('volcano_days_count', 'area_km2', 0),
    ('volcano_days_count', 'one', 0),
    ('flood_days_count', 'area_km2', 0),
    ('flood_days_count', 'one', 0)
on conflict (numerator, denominator) do update
set min = excluded.min;
