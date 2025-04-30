with
     validated_input as (select (:polygon)::geometry as geom),
     boxinput as (select ST_Envelope(v.geom) as bbox from validated_input as v),
     subdivision as (select ST_Subdivide(v.geom) geom from validated_input v),
     -- hexes, indicators_as_columns CTE:
     %s
select %s
from indicators_as_columns
