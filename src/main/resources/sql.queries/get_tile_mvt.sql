select ST_AsMVT(q, 'stats', 8192, 'geom', 'h3ind') as tile
from
    (
        select %s,
            ST_AsMVTGeom(geom, ST_TileEnvelope(:z, :x, :y), 8192, 64, true) as geom,
            h3index_to_bigint(h3) as h3ind
        from stat_h3
        where zoom = :resolution
          and geom && ST_TileEnvelope(:z, :x, :y)
    ) as q;