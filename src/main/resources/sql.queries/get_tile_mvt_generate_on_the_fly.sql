with hexes as materialized (select sg.geom, sg.h3, m.internal_id
                     from stat_h3_geom sg, (values %s) m(internal_id)
                     where sg.geom && ST_TileEnvelope(:z, :x, :y)
                       and sg.resolution = :resolution
                ),
    res as (select sg.geom, sg.h3, st.indicator_uuid, st.indicator_value
            from stat_h3_transposed st
            join hexes sg on (sg.h3 = st.h3 and sg.internal_id = st.indicator_uuid)
                )
select ST_AsMVT(q, 'stats', 8192, 'geom', 'h3ind') as tile
from (select
                  %s,
                  ST_AsMVTGeom(geom, ST_TileEnvelope(:z, :x, :y), 8192, 64, true) as geom,
                  h3index_to_bigint(h3) as h3ind
      from res
      group by geom, h3) q;
