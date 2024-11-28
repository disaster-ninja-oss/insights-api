with hexes as materialized (
    select
        h3,
        m.internal_id
     from h3_polygon_to_cells(
            st_transform(ST_TileEnvelope(:z, :x, :y, margin := 0.08), 4326), :resolution) h3,
          (values %s) m(internal_id)
),
    res as (select sg.h3, st.indicator_uuid, st.indicator_value
            from stat_h3_transposed st
            join hexes sg on (sg.h3 = st.h3 and sg.internal_id = st.indicator_uuid)
                )
select ST_AsMVT(q, 'stats', 8192, 'geom', 'h3ind') as tile
from (select
        %s,
        ST_AsMVTGeom(
            st_transform(h3_cell_to_boundary_geometry(h3), 3857),
            ST_TileEnvelope(:z, :x, :y), 8192, 64, true
        ) as geom,
        h3index_to_bigint(h3) as h3ind
      from res
      group by h3) q;
