--liquibase formatted sql
--changeset insights-api:add_table_bivariate_overlays splitStatements:false stripComments:false endDelimiter:; runOnChange:true
drop table if exists bivariate_overlays;

create table bivariate_overlays
(
    ord           float,
    name          text,
    description   text,
    x_numerator   text, -- vertical axis on DN
    x_denominator text, -- vertical axis on DN
    y_numerator   text, -- horizontal axis on DN
    y_denominator text, -- horizontal axis on DN
    active        boolean,
    colors        jsonb,
    application   json,
    is_public     boolean
);

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 1,
       'Kontur OpenStreetMap Quantity',
       'count',
       'area_km2',
       'population',
       'area_km2',
       true,
       E'This map shows relative distribution of OpenStreetMap objects and Population. \n\nLast updated ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(232,232,157)"},{"id":"A2","color":"rgb(239,163,127)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(186,226,153)"},{"id":"B2","color":"rgb(161,173,88)"},{"id":"B3","color":"rgb(191,108,63)"},
          {"id":"C1","color":"rgb(90,200,127)"},{"id":"C2","color":"rgb(112,186,128)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 2,
       'Kontur OpenStreetMap Building Quantity',
       'building_count',
       'area_km2',
       'population',
       'area_km2',
       false,
       E'This map shows whether all populated houses are mapped in OpenStreetMap. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(232,232,157)"},{"id":"A2","color":"rgb(239,163,127)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(186,226,153)"},{"id":"B2","color":"rgb(161,173,88)"},{"id":"B3","color":"rgb(191,108,63)"},
          {"id":"C1","color":"rgb(90,200,127)"},{"id":"C2","color":"rgb(112,186,128)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 3,
       'Kontur OpenStreetMap Road Completeness',
       'highway_length',
       'total_road_length',
       'population',
       'area_km2',
       false,
       E'This map shows whether all roads are mapped in OpenStreetMap. Road completeness is calculated as the ratio of OpenStreetMap road length to total estimated road length (OpenStreetMap + Facebook roads). For places where Facebook roads data are unavailable, the estimation is based on statistical regression from Kontur Population data. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(232,232,157)"},{"id":"A2","color":"rgb(239,163,127)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(186,226,153)"},{"id":"B2","color":"rgb(161,173,88)"},{"id":"B3","color":"rgb(191,108,63)"},
          {"id":"C1","color":"rgb(90,200,127)"},{"id":"C2","color":"rgb(112,186,128)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from osm_meta;;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 4,
       'Kontur OpenStreetMap Mapping Activity',
       'local_hours',
       'area_km2',
       'total_hours',
       'area_km2',
       false,
       E'Greener - stronger local community, darker - more active mapping. This map shows how active mapping in the area in last two years is. All mapping hours are shown against mapping hours we can surely attribute to an active local user. Mapper is considered active if they contributed more than 30 mapping hours during last two years. Position of active mapper is estimated by region of their highest activity. A mapping hour is a hour in which an user uploaded at least one tagged object. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(173,169,200)"},{"id":"A2","color":"rgb(122,113,178)"},{"id":"A3","color":"rgb(93,83,152)"},
          {"id":"B1","color":"rgb(157,183,181)"},{"id":"B2","color":"rgb(118,142,159)"},{"id":"B3","color":"rgb(88,118,129)"},
          {"id":"C1","color":"rgb(137,200,158)"},{"id":"C2","color":"rgb(113,178,135)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 5,
       'Kontur OpenStreetMap Antiquity',
       'avgmax_ts',
       'one',
       'view_count',
       'area_km2',
       false,
       E'This map shows how old is OpenStreetMap and how many times users view in particular region of OpenStreetMap for the last 30 days. Explore to find the least edited, but the most popular areas at the same time. \n\nLast updated ' ||
        max(tile_date) || E' \n\n',
        '[{"id":"A1","color":"rgb(232,232,157)"},{"id":"A2","color":"rgb(239,163,127)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(186,226,153)"},{"id":"B2","color":"rgb(161,173,88)"},{"id":"B3","color":"rgb(191,108,63)"},
          {"id":"C1","color":"rgb(90,200,127)"},{"id":"C2","color":"rgb(112,186,128)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from tile_logs;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 6,
       'Kontur Nighttime Heatwave Risk',
       'days_mintemp_above_25c_1c',
       'one',
       'population',
       'area_km2',
       false,
       E'This map shows heat-stress risk areas, where nighttime temperatures stay above 25°C. Due to the increase of greenhouse gases the nighttime temperatures are growing at a rate of 0.07 degrees per decade, compared to 0.05 degrees for daytime maximums globally (for the period from 1960 to 2009).
       The number of people exposed to nighttime heatwaves in particular regions is provided by Kontur. The current warming scenario of the average number of nights over 25°C during the year is based on data and other content made available by Probable Futures, a Project of SouthCoast Community Foundation, and certain of that data may have been provided to Probable Futures by Woodwell Climate Research Center, Inc. or The Coordinated Regional climate Downscaling Experiment (CORDEX). \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(103,176,100)"},{"id":"A2","color":"rgb(103,176,100)"},{"id":"A3","color":"rgb(103,176,100)"},
          {"id":"B1","color":"rgb(232,232,157)"},{"id":"B2","color":"rgb(228,185,129)"},{"id":"B3","color":"rgb(228,127,129)"},
          {"id":"C1","color":"rgb(232,232,157)"},{"id":"C2","color":"rgb(229,154,55)"},{"id":"C3","color":"rgb(228,26,28)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 7,
       'Kontur Fire Service Scarcity Risk',
       'man_distance_to_fire_brigade',
       'population',
       'population',
       'area_km2',
       false,
       E'This map shows areas at higher risk in case of a fire due to population density and fire station distance ratio. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(103,176,100)"},{"id":"A2","color":"rgb(103,176,100)"},{"id":"A3","color":"rgb(103,176,100)"},
          {"id":"B1","color":"rgb(232,232,157)"},{"id":"B2","color":"rgb(228,185,129)"},{"id":"B3","color":"rgb(228,127,129)"},
          {"id":"C1","color":"rgb(232,232,157)"},{"id":"C2","color":"rgb(229,154,55)"},{"id":"C3","color":"rgb(228,26,28)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 8,
       'Kontur OpenStreetMap Views before/after 24.02.22',
       'view_count_bf2402',
       'area_km2',
       'view_count',
       'area_km2',
       false,
       E'This map shows how many times users viewed OpenStreetMap in particular region for the last 30 days in comparison to 30 days before 24.02.2022. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(204,204,204)"},{"id":"A2","color":"rgb(206,154,151)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(136,135,222)"},{"id":"B2","color":"rgb(166,166,166)"},{"id":"B3","color":"rgb(180,80,75)"},
          {"id":"C1","color":"rgb(28,26,228)"},{"id":"C2","color":"rgb(78,77,178)"},{"id":"C3","color":"rgb(128,128,128)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 9,
       'EV Charging Availability',
       'man_distance_to_charging_stations',
       'population',
       'population',
       'area_km2',
       false,
       E'This map shows charging station coverage for electric vehicles (EVs). Green areas have a charger nearby.
        Red areas are populated and need a charging station to be constructed or added to the OpenStreetMap database if one already exists. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(134,198,136)"},{"id":"A2","color":"rgb(87,164,103)"},{"id":"A3","color":"rgb(36,133,67)"},
          {"id":"B1","color":"rgb(176,214,128)"},{"id":"B2","color":"rgb(208,159,97)"},{"id":"B3","color":"rgb(191,108,64)"},
          {"id":"C1","color":"rgb(232,232,157)"},{"id":"C2","color":"rgb(224,185,133)"},{"id":"C3","color":"rgb(228,26,28)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 10,
       'Waste containers availability',
       'waste_basket_coverage_area_km2',
       'populated_area_km2',
       'population',
       'area_km2',
       false,
       E'This map shows the availability of waste bins in populated areas. In a green zone, a large part of the populated area is covered with waste containers. Red zones are populated and require placing more waste containers in the area - or adding them to the OpenStreetMap database if they are already placed. Yellow zones are unpopulated and thus do not require waste collection. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(232,232,157)"},{"id":"A2","color":"rgb(239,163,127)"},{"id":"A3","color":"rgb(228,26,28)"},
          {"id":"B1","color":"rgb(186,226,153)"},{"id":"B2","color":"rgb(161,173,88)"},{"id":"B3","color":"rgb(191,108,63)"},
          {"id":"C1","color":"rgb(90,200,127)"},{"id":"C2","color":"rgb(112,186,128)"},{"id":"C3","color":"rgb(83,152,106)"}]',
       true
from osm_meta;

insert into bivariate_overlays (ord, name, x_numerator, x_denominator, y_numerator, y_denominator, active, description, colors, is_public)
select 11,
       'Shelters Scarcity Risk',
       'man_distance_to_bomb_shelters',
       'population',
       'population',
       'area_km2',
       false,
       E'The availability of strongly built shelters that give protection from extreme weather events or attacks is an indicator of community resilience.
       This map shows areas at risk of failing to protect inhabitants in case of emergencies due to the combination of high population density and long distance to the nearest bunker. \n\nLast updated  ' ||
       json_extract_path_text(meta::json, 'data', 'timestamp', 'last') || E' \n\n',
        '[{"id":"A1","color":"rgb(103,176,100)"},{"id":"A2","color":"rgb(103,176,100)"},{"id":"A3","color":"rgb(103,176,100)"},
          {"id":"B1","color":"rgb(232,232,157)"},{"id":"B2","color":"rgb(228,185,129)"},{"id":"B3","color":"rgb(228,127,129)"},
          {"id":"C1","color":"rgb(232,232,157)"},{"id":"C2","color":"rgb(229,154,55)"},{"id":"C3","color":"rgb(228,26,28)"}]',
       true
from osm_meta;
