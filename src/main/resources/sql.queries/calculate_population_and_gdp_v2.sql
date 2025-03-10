WITH validated_input AS (
    -- Input geometry can be very complex in real scenarios, up to thousands of points.
    -- This is coming from user and we have no real control over it.
    select :geometry::geometry as geom
),
input_bbox AS (
    -- For fast prefiltering using GIST index, generate a bbox for the input geometry
    SELECT ST_Envelope(geom) AS bbox
    FROM validated_input
),
input_subdivision AS (
    -- For fast filtering after the bbox check, subdivide the geometry.
    SELECT ST_Subdivide(geom, 64) AS geom
    FROM validated_input
),
hexes_in_area as materialized (
    -- Pick hexes that are present in the database that intersect the subdivided geometry
    SELECT DISTINCT sh.h3  
    FROM stat_h3_geom sh
    JOIN input_subdivision sb ON ST_Intersects(sh.geom, sb.geom)
    WHERE sh.resolution = 8
        AND sh.geom && (SELECT bbox FROM input_bbox LIMIT 1)
),
res AS (
    -- Extract values for hexes from available indicators
    SELECT st.h3, st.indicator_value, st.indicator_uuid
    FROM hexes_in_area
    JOIN stat_h3_transposed st USING(h3)
    WHERE
        indicator_uuid in (%s)
),
indicators_as_columns as (
    -- Pivot the table for easier summation.
    -- Deduplicate the h3's if we somehow got multiple above.
    -- ???: do we have to do it? seems to be only needed if we have two of the same indicators in READY state
    SELECT
        h3,
        COALESCE(MAX(indicator_value) FILTER (WHERE indicator_uuid = '%s'), 0) AS population,
        COALESCE(MAX(indicator_value) FILTER (WHERE indicator_uuid = '%s'), 0) AS gdp,
        COALESCE(MAX(indicator_value) FILTER (WHERE indicator_uuid = '%s'), 0) AS residential
    FROM res
    GROUP BY h3
)
-- Finally, summarize the results.
SELECT
    SUM(dh.population) AS population,
    SUM(dh.population * dh.residential) AS urban,
    SUM(dh.gdp) AS gdp,
    'population' AS type
FROM indicators_as_columns dh;
