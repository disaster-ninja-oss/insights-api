--liquibase formatted sql
--changeset insights-api:init_schema splitStatements:false stripComments:false endDelimiter:; runOnChange:false

CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS h3 WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS postgis_raster WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS h3_postgis WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS pg_stat_statements WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS pgrouting WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;

CREATE TABLE public.bivariate_axis_correlation_v2 (
    correlation double precision,
    quality double precision,
    x_numerator_id uuid,
    x_denominator_id uuid,
    y_numerator_id uuid,
    y_denominator_id uuid
);

CREATE TABLE public.bivariate_axis_overrides (
    numerator_id uuid NOT NULL,
    denominator_id uuid NOT NULL,
    label text,
    min double precision,
    p25 double precision,
    p75 double precision,
    max double precision,
    min_label text,
    p25_label text,
    p75_label text,
    max_label text
);

CREATE TABLE public.bivariate_axis_v2 (
    numerator text,
    denominator text,
    min double precision,
    p25 double precision,
    p75 double precision,
    max double precision,
    quality double precision,
    min_label text,
    p25_label text,
    p75_label text,
    max_label text,
    label text,
    sum_value double precision,
    sum_quality double precision,
    min_value double precision,
    min_quality double precision,
    max_value double precision,
    max_quality double precision,
    stddev_value double precision,
    stddev_quality double precision,
    median_value double precision,
    median_quality double precision,
    mean_value double precision,
    mean_quality double precision,
    numerator_uuid uuid,
    denominator_uuid uuid,
    default_transform jsonb,
    transformations jsonb,
    mean_all_res double precision,
    stddev_all_res double precision
);

CREATE TABLE public.bivariate_colors (
    color text,
    color_comment text,
    corner json
);

CREATE TABLE public.bivariate_indicators_metadata (
    param_id text NOT NULL,
    param_label text,
    copyrights json,
    direction json,
    is_base boolean DEFAULT false NOT NULL,
    internal_id uuid NOT NULL,
    owner text NOT NULL,
    state text,
    is_public boolean,
    allowed_users json,
    date timestamp with time zone,
    description text,
    coverage text,
    update_frequency text,
    application json,
    unit_id text,
    last_updated timestamp with time zone,
    external_id uuid NOT NULL,
    emoji text,
    upload_id uuid,
    max_res integer DEFAULT 8,
    downscale text,
    hash text
);

CREATE TABLE public.bivariate_unit (
    id text NOT NULL,
    type text,
    measurement text,
    is_base boolean
);

CREATE TABLE public.bivariate_unit_localization (
    unit_id text NOT NULL,
    language text,
    short_name text,
    long_name text
);

CREATE TABLE public.llm_cache (
    hash uuid NOT NULL,
    request text,
    response text,
    model_name text,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE public.nominatim_cache (
    query_hash uuid NOT NULL,
    query text,
    response jsonb,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE public.search_history (
    app_id uuid,
    query text,
    search_results jsonb,
    selected_feature jsonb,
    selected_feature_type text,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE public.shedlock (
    name character varying(64) NOT NULL,
    lock_until timestamp without time zone NOT NULL,
    locked_at timestamp without time zone NOT NULL,
    locked_by character varying(255) NOT NULL
);

CREATE TABLE public.stat_h3_geom (
    h3 public.h3index,
    resolution integer,
    geom public.geometry
);

CREATE TABLE public.stat_h3_transposed (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
)
PARTITION BY HASH (indicator_uuid);

CREATE TABLE public.stat_h3_transposed_p0 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p1 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p10 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p100 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p101 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p102 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p103 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p104 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p105 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p106 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p107 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p108 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p109 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p11 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p110 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p111 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p112 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p113 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p114 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p115 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p116 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p117 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p118 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p119 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p12 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p120 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p121 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p122 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p123 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p124 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p125 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p126 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p127 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p128 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p129 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p13 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p130 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p131 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p132 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p133 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p134 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p135 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p136 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p137 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p138 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p139 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p14 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p140 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p141 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p142 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p143 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p144 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p145 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p146 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p147 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p148 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p149 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p15 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p150 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p151 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p152 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p153 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p154 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p155 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p156 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p157 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p158 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p159 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p16 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p160 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p161 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p162 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p163 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p164 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p165 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p166 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p167 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p168 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p169 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p17 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p170 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p171 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p172 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p173 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p174 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p175 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p176 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p177 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p178 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p179 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p18 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p180 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p181 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p182 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p183 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p184 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p185 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p186 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p187 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p188 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p189 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p19 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p190 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p191 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p192 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p193 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p194 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p195 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p196 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p197 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p198 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p199 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p2 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p20 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p200 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p201 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p202 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p203 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p204 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p205 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p206 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p207 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p208 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p209 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p21 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p210 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p211 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p212 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p213 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p214 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p215 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p216 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p217 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p218 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p219 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p22 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p220 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p221 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p222 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p223 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p224 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p225 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p226 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p227 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p228 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p229 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p23 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p230 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p231 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p232 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p233 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p234 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p235 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p236 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p237 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p238 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p239 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p24 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p240 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p241 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p242 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p243 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p244 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p245 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p246 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p247 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p248 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p249 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p25 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p250 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p251 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p252 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p253 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p254 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p255 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p26 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p27 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p28 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p29 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p3 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p30 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p31 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p32 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p33 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p34 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p35 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p36 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p37 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p38 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p39 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p4 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p40 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p41 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p42 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p43 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p44 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p45 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p46 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p47 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p48 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p49 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p5 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p50 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p51 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p52 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p53 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p54 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p55 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p56 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p57 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p58 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p59 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p6 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p60 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p61 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p62 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p63 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p64 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p65 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p66 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p67 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p68 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p69 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p7 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p70 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p71 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p72 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p73 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p74 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p75 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p76 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p77 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p78 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p79 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p8 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p80 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p81 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p82 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p83 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p84 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p85 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p86 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p87 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p88 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p89 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p9 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p90 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p91 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p92 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p93 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p94 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p95 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p96 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p97 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p98 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.stat_h3_transposed_p99 (
    h3 public.h3index NOT NULL,
    indicator_uuid uuid NOT NULL,
    indicator_value double precision NOT NULL
);

CREATE TABLE public.task_queue (
    task_type text NOT NULL,
    x_numerator_id uuid,
    x_denominator_id uuid,
    y_numerator_id uuid,
    y_denominator_id uuid,
    priority double precision NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p0 FOR VALUES WITH (modulus 256, remainder 0);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p1 FOR VALUES WITH (modulus 256, remainder 1);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p10 FOR VALUES WITH (modulus 256, remainder 10);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p100 FOR VALUES WITH (modulus 256, remainder 100);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p101 FOR VALUES WITH (modulus 256, remainder 101);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p102 FOR VALUES WITH (modulus 256, remainder 102);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p103 FOR VALUES WITH (modulus 256, remainder 103);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p104 FOR VALUES WITH (modulus 256, remainder 104);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p105 FOR VALUES WITH (modulus 256, remainder 105);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p106 FOR VALUES WITH (modulus 256, remainder 106);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p107 FOR VALUES WITH (modulus 256, remainder 107);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p108 FOR VALUES WITH (modulus 256, remainder 108);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p109 FOR VALUES WITH (modulus 256, remainder 109);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p11 FOR VALUES WITH (modulus 256, remainder 11);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p110 FOR VALUES WITH (modulus 256, remainder 110);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p111 FOR VALUES WITH (modulus 256, remainder 111);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p112 FOR VALUES WITH (modulus 256, remainder 112);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p113 FOR VALUES WITH (modulus 256, remainder 113);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p114 FOR VALUES WITH (modulus 256, remainder 114);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p115 FOR VALUES WITH (modulus 256, remainder 115);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p116 FOR VALUES WITH (modulus 256, remainder 116);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p117 FOR VALUES WITH (modulus 256, remainder 117);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p118 FOR VALUES WITH (modulus 256, remainder 118);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p119 FOR VALUES WITH (modulus 256, remainder 119);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p12 FOR VALUES WITH (modulus 256, remainder 12);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p120 FOR VALUES WITH (modulus 256, remainder 120);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p121 FOR VALUES WITH (modulus 256, remainder 121);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p122 FOR VALUES WITH (modulus 256, remainder 122);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p123 FOR VALUES WITH (modulus 256, remainder 123);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p124 FOR VALUES WITH (modulus 256, remainder 124);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p125 FOR VALUES WITH (modulus 256, remainder 125);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p126 FOR VALUES WITH (modulus 256, remainder 126);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p127 FOR VALUES WITH (modulus 256, remainder 127);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p128 FOR VALUES WITH (modulus 256, remainder 128);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p129 FOR VALUES WITH (modulus 256, remainder 129);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p13 FOR VALUES WITH (modulus 256, remainder 13);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p130 FOR VALUES WITH (modulus 256, remainder 130);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p131 FOR VALUES WITH (modulus 256, remainder 131);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p132 FOR VALUES WITH (modulus 256, remainder 132);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p133 FOR VALUES WITH (modulus 256, remainder 133);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p134 FOR VALUES WITH (modulus 256, remainder 134);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p135 FOR VALUES WITH (modulus 256, remainder 135);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p136 FOR VALUES WITH (modulus 256, remainder 136);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p137 FOR VALUES WITH (modulus 256, remainder 137);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p138 FOR VALUES WITH (modulus 256, remainder 138);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p139 FOR VALUES WITH (modulus 256, remainder 139);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p14 FOR VALUES WITH (modulus 256, remainder 14);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p140 FOR VALUES WITH (modulus 256, remainder 140);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p141 FOR VALUES WITH (modulus 256, remainder 141);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p142 FOR VALUES WITH (modulus 256, remainder 142);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p143 FOR VALUES WITH (modulus 256, remainder 143);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p144 FOR VALUES WITH (modulus 256, remainder 144);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p145 FOR VALUES WITH (modulus 256, remainder 145);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p146 FOR VALUES WITH (modulus 256, remainder 146);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p147 FOR VALUES WITH (modulus 256, remainder 147);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p148 FOR VALUES WITH (modulus 256, remainder 148);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p149 FOR VALUES WITH (modulus 256, remainder 149);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p15 FOR VALUES WITH (modulus 256, remainder 15);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p150 FOR VALUES WITH (modulus 256, remainder 150);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p151 FOR VALUES WITH (modulus 256, remainder 151);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p152 FOR VALUES WITH (modulus 256, remainder 152);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p153 FOR VALUES WITH (modulus 256, remainder 153);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p154 FOR VALUES WITH (modulus 256, remainder 154);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p155 FOR VALUES WITH (modulus 256, remainder 155);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p156 FOR VALUES WITH (modulus 256, remainder 156);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p157 FOR VALUES WITH (modulus 256, remainder 157);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p158 FOR VALUES WITH (modulus 256, remainder 158);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p159 FOR VALUES WITH (modulus 256, remainder 159);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p16 FOR VALUES WITH (modulus 256, remainder 16);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p160 FOR VALUES WITH (modulus 256, remainder 160);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p161 FOR VALUES WITH (modulus 256, remainder 161);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p162 FOR VALUES WITH (modulus 256, remainder 162);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p163 FOR VALUES WITH (modulus 256, remainder 163);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p164 FOR VALUES WITH (modulus 256, remainder 164);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p165 FOR VALUES WITH (modulus 256, remainder 165);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p166 FOR VALUES WITH (modulus 256, remainder 166);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p167 FOR VALUES WITH (modulus 256, remainder 167);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p168 FOR VALUES WITH (modulus 256, remainder 168);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p169 FOR VALUES WITH (modulus 256, remainder 169);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p17 FOR VALUES WITH (modulus 256, remainder 17);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p170 FOR VALUES WITH (modulus 256, remainder 170);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p171 FOR VALUES WITH (modulus 256, remainder 171);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p172 FOR VALUES WITH (modulus 256, remainder 172);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p173 FOR VALUES WITH (modulus 256, remainder 173);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p174 FOR VALUES WITH (modulus 256, remainder 174);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p175 FOR VALUES WITH (modulus 256, remainder 175);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p176 FOR VALUES WITH (modulus 256, remainder 176);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p177 FOR VALUES WITH (modulus 256, remainder 177);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p178 FOR VALUES WITH (modulus 256, remainder 178);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p179 FOR VALUES WITH (modulus 256, remainder 179);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p18 FOR VALUES WITH (modulus 256, remainder 18);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p180 FOR VALUES WITH (modulus 256, remainder 180);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p181 FOR VALUES WITH (modulus 256, remainder 181);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p182 FOR VALUES WITH (modulus 256, remainder 182);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p183 FOR VALUES WITH (modulus 256, remainder 183);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p184 FOR VALUES WITH (modulus 256, remainder 184);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p185 FOR VALUES WITH (modulus 256, remainder 185);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p186 FOR VALUES WITH (modulus 256, remainder 186);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p187 FOR VALUES WITH (modulus 256, remainder 187);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p188 FOR VALUES WITH (modulus 256, remainder 188);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p189 FOR VALUES WITH (modulus 256, remainder 189);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p19 FOR VALUES WITH (modulus 256, remainder 19);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p190 FOR VALUES WITH (modulus 256, remainder 190);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p191 FOR VALUES WITH (modulus 256, remainder 191);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p192 FOR VALUES WITH (modulus 256, remainder 192);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p193 FOR VALUES WITH (modulus 256, remainder 193);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p194 FOR VALUES WITH (modulus 256, remainder 194);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p195 FOR VALUES WITH (modulus 256, remainder 195);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p196 FOR VALUES WITH (modulus 256, remainder 196);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p197 FOR VALUES WITH (modulus 256, remainder 197);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p198 FOR VALUES WITH (modulus 256, remainder 198);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p199 FOR VALUES WITH (modulus 256, remainder 199);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p2 FOR VALUES WITH (modulus 256, remainder 2);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p20 FOR VALUES WITH (modulus 256, remainder 20);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p200 FOR VALUES WITH (modulus 256, remainder 200);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p201 FOR VALUES WITH (modulus 256, remainder 201);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p202 FOR VALUES WITH (modulus 256, remainder 202);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p203 FOR VALUES WITH (modulus 256, remainder 203);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p204 FOR VALUES WITH (modulus 256, remainder 204);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p205 FOR VALUES WITH (modulus 256, remainder 205);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p206 FOR VALUES WITH (modulus 256, remainder 206);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p207 FOR VALUES WITH (modulus 256, remainder 207);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p208 FOR VALUES WITH (modulus 256, remainder 208);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p209 FOR VALUES WITH (modulus 256, remainder 209);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p21 FOR VALUES WITH (modulus 256, remainder 21);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p210 FOR VALUES WITH (modulus 256, remainder 210);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p211 FOR VALUES WITH (modulus 256, remainder 211);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p212 FOR VALUES WITH (modulus 256, remainder 212);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p213 FOR VALUES WITH (modulus 256, remainder 213);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p214 FOR VALUES WITH (modulus 256, remainder 214);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p215 FOR VALUES WITH (modulus 256, remainder 215);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p216 FOR VALUES WITH (modulus 256, remainder 216);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p217 FOR VALUES WITH (modulus 256, remainder 217);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p218 FOR VALUES WITH (modulus 256, remainder 218);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p219 FOR VALUES WITH (modulus 256, remainder 219);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p22 FOR VALUES WITH (modulus 256, remainder 22);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p220 FOR VALUES WITH (modulus 256, remainder 220);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p221 FOR VALUES WITH (modulus 256, remainder 221);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p222 FOR VALUES WITH (modulus 256, remainder 222);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p223 FOR VALUES WITH (modulus 256, remainder 223);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p224 FOR VALUES WITH (modulus 256, remainder 224);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p225 FOR VALUES WITH (modulus 256, remainder 225);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p226 FOR VALUES WITH (modulus 256, remainder 226);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p227 FOR VALUES WITH (modulus 256, remainder 227);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p228 FOR VALUES WITH (modulus 256, remainder 228);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p229 FOR VALUES WITH (modulus 256, remainder 229);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p23 FOR VALUES WITH (modulus 256, remainder 23);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p230 FOR VALUES WITH (modulus 256, remainder 230);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p231 FOR VALUES WITH (modulus 256, remainder 231);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p232 FOR VALUES WITH (modulus 256, remainder 232);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p233 FOR VALUES WITH (modulus 256, remainder 233);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p234 FOR VALUES WITH (modulus 256, remainder 234);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p235 FOR VALUES WITH (modulus 256, remainder 235);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p236 FOR VALUES WITH (modulus 256, remainder 236);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p237 FOR VALUES WITH (modulus 256, remainder 237);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p238 FOR VALUES WITH (modulus 256, remainder 238);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p239 FOR VALUES WITH (modulus 256, remainder 239);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p24 FOR VALUES WITH (modulus 256, remainder 24);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p240 FOR VALUES WITH (modulus 256, remainder 240);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p241 FOR VALUES WITH (modulus 256, remainder 241);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p242 FOR VALUES WITH (modulus 256, remainder 242);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p243 FOR VALUES WITH (modulus 256, remainder 243);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p244 FOR VALUES WITH (modulus 256, remainder 244);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p245 FOR VALUES WITH (modulus 256, remainder 245);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p246 FOR VALUES WITH (modulus 256, remainder 246);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p247 FOR VALUES WITH (modulus 256, remainder 247);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p248 FOR VALUES WITH (modulus 256, remainder 248);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p249 FOR VALUES WITH (modulus 256, remainder 249);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p25 FOR VALUES WITH (modulus 256, remainder 25);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p250 FOR VALUES WITH (modulus 256, remainder 250);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p251 FOR VALUES WITH (modulus 256, remainder 251);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p252 FOR VALUES WITH (modulus 256, remainder 252);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p253 FOR VALUES WITH (modulus 256, remainder 253);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p254 FOR VALUES WITH (modulus 256, remainder 254);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p255 FOR VALUES WITH (modulus 256, remainder 255);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p26 FOR VALUES WITH (modulus 256, remainder 26);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p27 FOR VALUES WITH (modulus 256, remainder 27);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p28 FOR VALUES WITH (modulus 256, remainder 28);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p29 FOR VALUES WITH (modulus 256, remainder 29);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p3 FOR VALUES WITH (modulus 256, remainder 3);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p30 FOR VALUES WITH (modulus 256, remainder 30);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p31 FOR VALUES WITH (modulus 256, remainder 31);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p32 FOR VALUES WITH (modulus 256, remainder 32);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p33 FOR VALUES WITH (modulus 256, remainder 33);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p34 FOR VALUES WITH (modulus 256, remainder 34);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p35 FOR VALUES WITH (modulus 256, remainder 35);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p36 FOR VALUES WITH (modulus 256, remainder 36);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p37 FOR VALUES WITH (modulus 256, remainder 37);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p38 FOR VALUES WITH (modulus 256, remainder 38);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p39 FOR VALUES WITH (modulus 256, remainder 39);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p4 FOR VALUES WITH (modulus 256, remainder 4);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p40 FOR VALUES WITH (modulus 256, remainder 40);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p41 FOR VALUES WITH (modulus 256, remainder 41);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p42 FOR VALUES WITH (modulus 256, remainder 42);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p43 FOR VALUES WITH (modulus 256, remainder 43);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p44 FOR VALUES WITH (modulus 256, remainder 44);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p45 FOR VALUES WITH (modulus 256, remainder 45);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p46 FOR VALUES WITH (modulus 256, remainder 46);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p47 FOR VALUES WITH (modulus 256, remainder 47);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p48 FOR VALUES WITH (modulus 256, remainder 48);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p49 FOR VALUES WITH (modulus 256, remainder 49);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p5 FOR VALUES WITH (modulus 256, remainder 5);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p50 FOR VALUES WITH (modulus 256, remainder 50);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p51 FOR VALUES WITH (modulus 256, remainder 51);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p52 FOR VALUES WITH (modulus 256, remainder 52);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p53 FOR VALUES WITH (modulus 256, remainder 53);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p54 FOR VALUES WITH (modulus 256, remainder 54);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p55 FOR VALUES WITH (modulus 256, remainder 55);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p56 FOR VALUES WITH (modulus 256, remainder 56);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p57 FOR VALUES WITH (modulus 256, remainder 57);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p58 FOR VALUES WITH (modulus 256, remainder 58);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p59 FOR VALUES WITH (modulus 256, remainder 59);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p6 FOR VALUES WITH (modulus 256, remainder 6);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p60 FOR VALUES WITH (modulus 256, remainder 60);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p61 FOR VALUES WITH (modulus 256, remainder 61);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p62 FOR VALUES WITH (modulus 256, remainder 62);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p63 FOR VALUES WITH (modulus 256, remainder 63);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p64 FOR VALUES WITH (modulus 256, remainder 64);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p65 FOR VALUES WITH (modulus 256, remainder 65);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p66 FOR VALUES WITH (modulus 256, remainder 66);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p67 FOR VALUES WITH (modulus 256, remainder 67);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p68 FOR VALUES WITH (modulus 256, remainder 68);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p69 FOR VALUES WITH (modulus 256, remainder 69);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p7 FOR VALUES WITH (modulus 256, remainder 7);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p70 FOR VALUES WITH (modulus 256, remainder 70);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p71 FOR VALUES WITH (modulus 256, remainder 71);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p72 FOR VALUES WITH (modulus 256, remainder 72);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p73 FOR VALUES WITH (modulus 256, remainder 73);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p74 FOR VALUES WITH (modulus 256, remainder 74);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p75 FOR VALUES WITH (modulus 256, remainder 75);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p76 FOR VALUES WITH (modulus 256, remainder 76);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p77 FOR VALUES WITH (modulus 256, remainder 77);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p78 FOR VALUES WITH (modulus 256, remainder 78);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p79 FOR VALUES WITH (modulus 256, remainder 79);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p8 FOR VALUES WITH (modulus 256, remainder 8);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p80 FOR VALUES WITH (modulus 256, remainder 80);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p81 FOR VALUES WITH (modulus 256, remainder 81);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p82 FOR VALUES WITH (modulus 256, remainder 82);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p83 FOR VALUES WITH (modulus 256, remainder 83);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p84 FOR VALUES WITH (modulus 256, remainder 84);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p85 FOR VALUES WITH (modulus 256, remainder 85);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p86 FOR VALUES WITH (modulus 256, remainder 86);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p87 FOR VALUES WITH (modulus 256, remainder 87);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p88 FOR VALUES WITH (modulus 256, remainder 88);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p89 FOR VALUES WITH (modulus 256, remainder 89);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p9 FOR VALUES WITH (modulus 256, remainder 9);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p90 FOR VALUES WITH (modulus 256, remainder 90);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p91 FOR VALUES WITH (modulus 256, remainder 91);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p92 FOR VALUES WITH (modulus 256, remainder 92);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p93 FOR VALUES WITH (modulus 256, remainder 93);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p94 FOR VALUES WITH (modulus 256, remainder 94);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p95 FOR VALUES WITH (modulus 256, remainder 95);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p96 FOR VALUES WITH (modulus 256, remainder 96);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p97 FOR VALUES WITH (modulus 256, remainder 97);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p98 FOR VALUES WITH (modulus 256, remainder 98);

ALTER TABLE ONLY public.stat_h3_transposed ATTACH PARTITION public.stat_h3_transposed_p99 FOR VALUES WITH (modulus 256, remainder 99);

ALTER TABLE ONLY public.bivariate_axis_correlation_v2
    ADD CONSTRAINT bivariate_axis_correlation_v2_unique_key UNIQUE (x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);

ALTER TABLE ONLY public.bivariate_axis_overrides
    ADD CONSTRAINT bivariate_axis_overrides_inique_key UNIQUE (numerator_id, denominator_id);

ALTER TABLE ONLY public.bivariate_axis_v2
    ADD CONSTRAINT bivariate_axis_v2_unique UNIQUE (numerator_uuid, denominator_uuid);

ALTER TABLE ONLY public.bivariate_indicators_metadata
    ADD CONSTRAINT bivariate_indicators_metadata_pk PRIMARY KEY (internal_id);

ALTER TABLE ONLY public.bivariate_unit_localization
    ADD CONSTRAINT bivariate_unit_localization_pkey PRIMARY KEY (unit_id);

ALTER TABLE ONLY public.bivariate_unit
    ADD CONSTRAINT bivariate_unit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.llm_cache
    ADD CONSTRAINT llm_cache_pkey PRIMARY KEY (hash);

ALTER TABLE ONLY public.nominatim_cache
    ADD CONSTRAINT nominatim_cache_pkey PRIMARY KEY (query_hash);

ALTER TABLE ONLY public.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);

ALTER TABLE ONLY public.task_queue
    ADD CONSTRAINT task_queue_unique UNIQUE NULLS NOT DISTINCT (task_type, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id);

CREATE INDEX stat_h3_geom_geom_resolution_idx ON public.stat_h3_geom USING gist (geom, resolution);

CREATE INDEX stat_h3_geom_h3_idx ON public.stat_h3_geom USING btree (h3);

CREATE INDEX stat_h3_transposed_partitioned_h3_indicator_uuid_idx ON ONLY public.stat_h3_transposed USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p0_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p0 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p100_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p100 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p101_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p101 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p102_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p102 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p103_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p103 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p104_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p104 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p105_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p105 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p106_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p106 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p107_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p107 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p108_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p108 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p109_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p109 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p10_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p10 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p110_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p110 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p111_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p111 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p112_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p112 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p113_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p113 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p114_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p114 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p115_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p115 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p116_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p116 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p117_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p117 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p118_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p118 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p119_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p119 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p11_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p11 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p120_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p120 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p121_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p121 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p122_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p122 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p123_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p123 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p124_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p124 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p125_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p125 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p126_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p126 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p127_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p127 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p128_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p128 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p129_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p129 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p12_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p12 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p130_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p130 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p131_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p131 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p132_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p132 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p133_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p133 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p134_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p134 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p135_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p135 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p136_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p136 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p137_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p137 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p138_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p138 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p139_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p139 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p13_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p13 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p140_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p140 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p141_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p141 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p142_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p142 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p143_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p143 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p144_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p144 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p145_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p145 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p146_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p146 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p147_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p147 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p148_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p148 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p149_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p149 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p14_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p14 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p150_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p150 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p151_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p151 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p152_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p152 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p153_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p153 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p154_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p154 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p155_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p155 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p156_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p156 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p157_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p157 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p158_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p158 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p159_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p159 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p15_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p15 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p160_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p160 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p161_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p161 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p162_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p162 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p163_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p163 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p164_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p164 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p165_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p165 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p166_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p166 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p167_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p167 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p168_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p168 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p169_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p169 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p16_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p16 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p170_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p170 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p171_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p171 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p172_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p172 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p173_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p173 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p174_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p174 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p175_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p175 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p176_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p176 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p177_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p177 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p178_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p178 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p179_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p179 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p17_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p17 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p180_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p180 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p181_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p181 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p182_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p182 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p183_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p183 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p184_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p184 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p185_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p185 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p186_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p186 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p187_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p187 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p188_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p188 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p189_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p189 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p18_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p18 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p190_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p190 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p191_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p191 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p192_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p192 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p193_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p193 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p194_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p194 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p195_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p195 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p196_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p196 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p197_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p197 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p198_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p198 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p199_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p199 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p19_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p19 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p1_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p1 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p200_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p200 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p201_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p201 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p202_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p202 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p203_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p203 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p204_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p204 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p205_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p205 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p206_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p206 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p207_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p207 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p208_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p208 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p209_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p209 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p20_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p20 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p210_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p210 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p211_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p211 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p212_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p212 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p213_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p213 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p214_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p214 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p215_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p215 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p216_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p216 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p217_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p217 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p218_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p218 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p219_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p219 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p21_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p21 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p220_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p220 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p221_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p221 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p222_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p222 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p223_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p223 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p224_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p224 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p225_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p225 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p226_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p226 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p227_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p227 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p228_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p228 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p229_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p229 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p22_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p22 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p230_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p230 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p231_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p231 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p232_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p232 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p233_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p233 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p234_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p234 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p235_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p235 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p236_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p236 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p237_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p237 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p238_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p238 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p239_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p239 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p23_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p23 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p240_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p240 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p241_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p241 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p242_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p242 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p243_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p243 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p244_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p244 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p245_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p245 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p246_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p246 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p247_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p247 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p248_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p248 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p249_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p249 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p24_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p24 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p250_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p250 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p251_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p251 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p252_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p252 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p253_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p253 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p254_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p254 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p255_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p255 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p25_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p25 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p26_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p26 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p27_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p27 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p28_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p28 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p29_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p29 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p2_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p2 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p30_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p30 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p31_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p31 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p32_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p32 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p33_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p33 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p34_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p34 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p35_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p35 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p36_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p36 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p37_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p37 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p38_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p38 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p39_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p39 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p3_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p3 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p40_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p40 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p41_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p41 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p42_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p42 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p43_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p43 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p44_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p44 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p45_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p45 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p46_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p46 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p47_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p47 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p48_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p48 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p49_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p49 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p4_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p4 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p50_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p50 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p51_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p51 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p52_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p52 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p53_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p53 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p54_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p54 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p55_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p55 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p56_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p56 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p57_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p57 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p58_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p58 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p59_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p59 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p5_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p5 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p60_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p60 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p61_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p61 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p62_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p62 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p63_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p63 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p64_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p64 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p65_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p65 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p66_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p66 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p67_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p67 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p68_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p68 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p69_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p69 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p6_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p6 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p70_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p70 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p71_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p71 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p72_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p72 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p73_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p73 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p74_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p74 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p75_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p75 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p76_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p76 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p77_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p77 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p78_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p78 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p79_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p79 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p7_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p7 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p80_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p80 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p81_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p81 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p82_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p82 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p83_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p83 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p84_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p84 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p85_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p85 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p86_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p86 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p87_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p87 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p88_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p88 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p89_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p89 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p8_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p8 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p90_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p90 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p91_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p91 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p92_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p92 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p93_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p93 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p94_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p94 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p95_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p95 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p96_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p96 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p97_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p97 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p98_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p98 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p99_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p99 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

CREATE INDEX stat_h3_transposed_p9_h3_indicator_uuid_indicator_value_idx ON public.stat_h3_transposed_p9 USING btree (h3, indicator_uuid) INCLUDE (indicator_value);

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p0_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p100_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p101_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p102_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p103_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p104_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p105_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p106_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p107_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p108_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p109_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p10_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p110_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p111_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p112_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p113_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p114_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p115_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p116_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p117_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p118_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p119_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p11_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p120_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p121_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p122_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p123_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p124_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p125_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p126_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p127_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p128_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p129_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p12_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p130_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p131_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p132_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p133_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p134_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p135_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p136_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p137_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p138_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p139_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p13_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p140_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p141_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p142_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p143_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p144_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p145_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p146_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p147_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p148_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p149_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p14_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p150_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p151_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p152_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p153_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p154_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p155_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p156_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p157_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p158_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p159_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p15_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p160_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p161_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p162_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p163_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p164_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p165_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p166_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p167_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p168_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p169_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p16_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p170_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p171_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p172_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p173_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p174_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p175_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p176_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p177_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p178_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p179_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p17_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p180_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p181_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p182_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p183_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p184_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p185_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p186_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p187_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p188_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p189_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p18_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p190_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p191_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p192_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p193_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p194_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p195_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p196_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p197_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p198_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p199_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p19_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p1_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p200_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p201_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p202_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p203_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p204_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p205_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p206_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p207_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p208_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p209_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p20_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p210_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p211_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p212_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p213_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p214_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p215_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p216_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p217_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p218_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p219_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p21_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p220_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p221_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p222_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p223_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p224_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p225_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p226_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p227_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p228_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p229_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p22_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p230_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p231_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p232_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p233_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p234_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p235_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p236_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p237_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p238_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p239_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p23_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p240_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p241_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p242_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p243_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p244_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p245_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p246_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p247_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p248_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p249_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p24_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p250_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p251_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p252_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p253_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p254_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p255_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p25_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p26_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p27_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p28_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p29_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p2_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p30_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p31_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p32_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p33_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p34_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p35_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p36_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p37_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p38_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p39_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p3_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p40_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p41_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p42_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p43_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p44_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p45_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p46_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p47_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p48_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p49_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p4_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p50_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p51_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p52_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p53_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p54_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p55_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p56_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p57_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p58_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p59_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p5_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p60_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p61_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p62_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p63_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p64_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p65_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p66_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p67_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p68_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p69_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p6_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p70_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p71_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p72_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p73_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p74_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p75_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p76_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p77_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p78_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p79_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p7_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p80_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p81_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p82_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p83_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p84_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p85_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p86_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p87_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p88_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p89_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p8_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p90_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p91_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p92_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p93_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p94_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p95_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p96_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p97_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p98_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p99_h3_indicator_uuid_indicator_value_idx;

ALTER INDEX public.stat_h3_transposed_partitioned_h3_indicator_uuid_idx ATTACH PARTITION public.stat_h3_transposed_p9_h3_indicator_uuid_indicator_value_idx;

ALTER TABLE ONLY public.bivariate_axis_correlation_v2
    ADD CONSTRAINT fk_bivariate_axis_correlation_v2_x_denominator_id FOREIGN KEY (x_denominator_id) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

ALTER TABLE ONLY public.bivariate_axis_correlation_v2
    ADD CONSTRAINT fk_bivariate_axis_correlation_v2_x_numerator_id FOREIGN KEY (x_numerator_id) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

ALTER TABLE ONLY public.bivariate_axis_correlation_v2
    ADD CONSTRAINT fk_bivariate_axis_correlation_v2_y_denominator_id FOREIGN KEY (y_denominator_id) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

ALTER TABLE ONLY public.bivariate_axis_correlation_v2
    ADD CONSTRAINT fk_bivariate_axis_correlation_v2_y_numerator_id FOREIGN KEY (y_numerator_id) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

ALTER TABLE ONLY public.bivariate_axis_v2
    ADD CONSTRAINT fk_bivariate_axis_v2_denominator_uuid FOREIGN KEY (denominator_uuid) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

ALTER TABLE ONLY public.bivariate_axis_v2
    ADD CONSTRAINT fk_bivariate_axis_v2_numerator_uuid FOREIGN KEY (numerator_uuid) REFERENCES public.bivariate_indicators_metadata(internal_id) ON DELETE CASCADE;

