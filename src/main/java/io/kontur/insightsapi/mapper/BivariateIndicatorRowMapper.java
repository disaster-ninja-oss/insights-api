package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BivariateIndicatorRowMapper implements RowMapper<BivariateIndicatorDto> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public BivariateIndicatorDto mapRow(ResultSet resultSet, int rowNum) {
        BivariateIndicatorDto bivariateIndicatorDto = new BivariateIndicatorDto();
        bivariateIndicatorDto.setId(resultSet.getString(BivariateIndicatorsColumns.param_id.name()));
        bivariateIndicatorDto.setLabel(resultSet.getString(BivariateIndicatorsColumns.param_label.name()));
        bivariateIndicatorDto.setCopyrights(resultSet.getString(BivariateIndicatorsColumns.copyrights.name()) == null
                ? null
                : objectMapper.readValue(resultSet.getString(BivariateIndicatorsColumns.copyrights.name()), new TypeReference<List<String>>(){}));
        bivariateIndicatorDto.setDirection(resultSet.getString(BivariateIndicatorsColumns.direction.name()) == null
                ? null
                : objectMapper.readValue(resultSet.getString(BivariateIndicatorsColumns.direction.name()), new TypeReference<List<List<String>>>(){}));
        bivariateIndicatorDto.setIsBase(resultSet.getBoolean(BivariateIndicatorsColumns.is_base.name()));
        bivariateIndicatorDto.setUuid(resultSet.getString(BivariateIndicatorsColumns.param_uuid.name()));
        bivariateIndicatorDto.setOwner(resultSet.getString(BivariateIndicatorsColumns.owner.name()));
        bivariateIndicatorDto.setState(resultSet.getString(BivariateIndicatorsColumns.state.name()));
        bivariateIndicatorDto.setIsPublic(resultSet.getBoolean(BivariateIndicatorsColumns.is_public.name()));
        bivariateIndicatorDto.setAllowedUsers(resultSet.getString(BivariateIndicatorsColumns.allowed_users.name()) == null
                ? null
                : objectMapper.readValue(resultSet.getString(BivariateIndicatorsColumns.allowed_users.name()), new TypeReference<List<String>>(){}));
        bivariateIndicatorDto.setDate(resultSet.getObject(BivariateIndicatorsColumns.date.name(), OffsetDateTime.class));
        bivariateIndicatorDto.setDescription(resultSet.getString(BivariateIndicatorsColumns.description.name()));
        bivariateIndicatorDto.setCoverage(resultSet.getString(BivariateIndicatorsColumns.coverage.name()));
        bivariateIndicatorDto.setUpdateFrequency(resultSet.getString(BivariateIndicatorsColumns.update_frequency.name()));
        bivariateIndicatorDto.setApplication(resultSet.getString(BivariateIndicatorsColumns.application.name()) == null
                ? null
                : objectMapper.readValue(resultSet.getString(BivariateIndicatorsColumns.application.name()), new TypeReference<List<String>>(){}));
        bivariateIndicatorDto.setUnitId(resultSet.getString(BivariateIndicatorsColumns.unit_id.name()));
        bivariateIndicatorDto.setLastUpdated(resultSet.getObject(BivariateIndicatorsColumns.last_updated.name(), OffsetDateTime.class));
        return bivariateIndicatorDto;
    }

    private enum BivariateIndicatorsColumns {
        param_id, param_label, copyrights, direction, is_base, param_uuid, owner, state, is_public,
        allowed_users, date, description, coverage, update_frequency, application, unit_id, last_updated
    }
}
