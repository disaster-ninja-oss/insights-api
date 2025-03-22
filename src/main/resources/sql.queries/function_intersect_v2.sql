with 
     validated_input as (select (:polygon)::geometry as geom),
     boxinput as (select ST_Envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select ST_Subdivide(v.geom) geom from validated_input v),
     hexes as (select distinct sh.h3
             from 
                stat_h3_geom sh,
                subdivision sb
             where 
                sh.resolution = 8
                and sh.geom && (select bbox from boxinput)
                and ST_Intersects(sh.geom, sb.geom)                
     ),
     res as (select %s from hexes sh
                      %s
     )
select %s
from res st;
