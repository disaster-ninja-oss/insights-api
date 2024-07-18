select jsonb_array_elements(transformations)
from bivariate_axis_v2
where numerator_uuid = (select internal_id from bivariate_indicators_metadata where param_id = ? and state = 'READY' and is_public limit 1)
and denominator_uuid = (select internal_id from bivariate_indicators_metadata where param_id = ? and state = 'READY' and is_public limit 1)
