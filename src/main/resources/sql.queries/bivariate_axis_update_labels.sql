update %s a
set
    label = b.label
from bivariate_axis_overrides b
where
    a.numerator_uuid = :numerator_uuid::uuid and
    a.denominator_uuid = :denominator_uuid::uuid and
    b.owner = :owner and
    a.numerator = b.numerator and
    a.denominator = b.denominator;
