package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.AxisOverridesRequest;
import io.kontur.insightsapi.dto.PresetDto;
import io.kontur.insightsapi.mapper.AxisRowMapper;
import io.kontur.insightsapi.mapper.TransformationRowMapper;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.Transformation;
import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final IndicatorRepository indicatorRepository;
    private final AxisRowMapper axisRowMapper;
    private final TransformationRowMapper transformationRowMapper;

    @Value("${calculations.useStatSeparateTables:false}")
    private Boolean useStatSeparateTables;

    @Value("classpath:/sql.queries/transformation_info.sql")
    private Resource transformationInfo;

    @Value("classpath:/sql.queries/axis_info.sql")
    private Resource axisInfo;

    @Transactional(readOnly = true)
    public List<Transformation> getTransformations(String numerator, String denominator) {
        if (!useStatSeparateTables) {
            return new ArrayList<>();
        }
        return jdbcTemplate.query(queryFactory.getSql(transformationInfo), transformationRowMapper, numerator, denominator);
    }

    @Transactional(readOnly = true)
    public List<Axis> getAxes() {
        if (!useStatSeparateTables) {
            return new ArrayList<>();
        }
        return jdbcTemplate.query(queryFactory.getSql(axisInfo), axisRowMapper);
    }

    public void validateIndicators(List<String> uuids, String owner) {
        for (String uuid : uuids)
            if (indicatorRepository.getIndicatorByOwnerAndExternalId(owner, uuid) == null)
                throw new IllegalArgumentException(String.format("No indicator %s for user %s", uuid, owner));
    }

    public void insertPreset(PresetDto preset, String owner)
            throws IllegalArgumentException {
        validateIndicators(List.of(
            preset.getX_numerator_id(),
            preset.getX_denominator_id(),
            preset.getY_numerator_id(),
            preset.getY_denominator_id()), owner);
        String sql = """
                insert into bivariate_overlays_v2
                (ord, name, description, x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id,
                 active, colors, application, is_public)
                values
                (?, ?, ?, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, ?::jsonb, ?::json, ?)
                on conflict (x_numerator_id, x_denominator_id, y_numerator_id, y_denominator_id) do update
                set
                    ord = excluded.ord,
                    name = excluded.name,
                    description = excluded.description,
                    active = excluded.active,
                    colors = excluded.colors,
                    application = excluded.application,
                    is_public = excluded.is_public
        """;
        try {
            jdbcTemplate.update(
                sql,
                preset.getOrd(),
                preset.getName(),
                preset.getDescription(),
                preset.getX_numerator_id(),
                preset.getX_denominator_id(),
                preset.getY_numerator_id(),
                preset.getY_denominator_id(),
                preset.getActive(),
                preset.getColors(),
                preset.getApplication(),
                preset.getIs_public()
                );
        } catch (Exception e) {
            logger.error("Could not update preset.", e);
            throw new IllegalArgumentException("Could not update preset.", e);
        }
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
        } catch (Exception e) {
            logger.error("Could not update bivariate_axis_overrides.", e);
            throw new IllegalArgumentException("Could not update bivariate_axis_overrides.", e);
        }
    }
}
