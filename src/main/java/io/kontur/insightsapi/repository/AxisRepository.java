package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.AxisOverridesRequest;
import io.kontur.insightsapi.dto.PresetDto;
import io.kontur.insightsapi.mapper.AxisRowMapper;
import io.kontur.insightsapi.mapper.TransformationRowMapper;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.Indicator;
import io.kontur.insightsapi.model.Transformation;
import io.kontur.insightsapi.repository.IndicatorRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AxisRepository {

    private static final Logger logger = LoggerFactory.getLogger(AxisRepository.class);

    private final QueryFactory queryFactory;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("writeJdbcTemplate")
    private final JdbcTemplate jdbcTemplateRW;

    private final IndicatorRepository indicatorRepository;
    private final TileRepository tileRepository;

    private final AxisRowMapper axisRowMapper;
    private final TransformationRowMapper transformationRowMapper;

    @Value("classpath:/sql.queries/transformation_info.sql")
    private Resource transformationInfo;

    @Value("classpath:/sql.queries/axis_info.sql")
    private Resource axisInfo;

    @Transactional(readOnly = true)
    public List<Transformation> getTransformations(String numerator, String denominator) {
        return jdbcTemplate.query(queryFactory.getSql(transformationInfo), transformationRowMapper, numerator, denominator);
    }

    @Transactional(readOnly = true)
    public List<Axis> getAxes() {
        List<Axis> axes = jdbcTemplate.query(queryFactory.getSql(axisInfo), axisRowMapper);
        Map<Integer, Integer> resolutionToZoom = getZoomMapping();
        for (Axis axis : axes) {
            for (Indicator quotient : axis.getQuotients()) {
                quotient.setMaxZoom(resolutionToZoom.get(quotient.getMaxRes()));
            }
        }
        return axes;
    }

    public Map<Integer, Integer> getZoomMapping() {
        Map<Integer, Integer> resolutionToZoom = new HashMap<>();

        // Initialize the mapping from zoom to resolution
        Map<Integer, Integer> zoomToResolution = tileRepository.initZoomToH3Resolutions();

        // Reverse the mapping
        for (Map.Entry<Integer, Integer> entry : zoomToResolution.entrySet()) {
            resolutionToZoom.merge(entry.getValue(), entry.getKey(), Math::min);
        }

        return resolutionToZoom;
    }

    public void validateIndicators(List<String> uuids, String owner) {
        for (String uuid : uuids)
            if (indicatorRepository.getIndicatorByOwnerAndExternalId(owner, uuid) == null)
                throw new IllegalArgumentException(String.format("No indicator %s for user %s", uuid, owner));
    }

    public void insertOverrides(AxisOverridesRequest request, String owner)
            throws IllegalArgumentException {
        String numerator = request.getNumerator_id();
        String denominator = request.getDenominator_id();
        validateIndicators(List.of(numerator, denominator), owner);

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
            jdbcTemplateRW.update(
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
        } catch (Exception e) {
            logger.error("Could not update bivariate_axis_overrides.", e);
            throw new IllegalArgumentException("Could not update bivariate_axis_overrides.", e);
        }
    }
}
