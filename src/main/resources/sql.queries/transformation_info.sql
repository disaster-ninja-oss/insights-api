select jsonb_array_elements(transformations)
from bivariate_axis_v2
where numerator_uuid = ?::uuid and denominator_uuid = ?::uuid
