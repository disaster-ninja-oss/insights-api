package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.AxisOverridesRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;

@Repository
@RequiredArgsConstructor
public class AxisRepository {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public void insertOverrides(AxisOverridesRequest request)
            throws IllegalArgumentException {
        String numerator = request.getNumerator_id();
        String denominator = request.getDenominator_id();
        String sql = """
                insert into bivariate_axis_overrides
                (numerator_id, denominator_id, label, min, max, p25, p75, min_label, p25_label, p75_label, max_label)
                values
                (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (numerator_id, denominator_id) do update
                set
                    label = excluded.label,
                    min = excluded.min,
                    max = excluded.max,
                    p25 = excluded.p25,
                    p75 = excluded.p75,
                    min_label = excluded.min_label,
                    max_label = excluded.max_label,
                    p25_label = excluded.p25_label,
                    p75_label = excluded.p75_label
        """;
        try {
            jdbcTemplate.update(
                sql,
                numerator,
                denominator,
                request.getLabel(),
                request.getMin(),
                request.getMax(),
                request.getP25(),
                request.getP75(),
                request.getMinLabel(),
                request.getP25Label(),
                request.getP75Label(),
                request.getMaxLabel()
                );
        } catch (DataIntegrityViolationException e) {
            logger.error("Could not update bivariate_axis_overrides due to FK constraint", e);
            // not-null constraint violation is also DataIntegrityViolationException, but we catch it earlier
            throw new IllegalArgumentException(
                    String.format("Could not apply overrides: some provided indicator IDs are missing in DB."));
        } catch (Exception e) {
            logger.error("Could not update bivariate_axis_overrides.", e);
            throw new IllegalArgumentException("Could not update bivariate_axis_overrides.", e);
        }
    }
}
