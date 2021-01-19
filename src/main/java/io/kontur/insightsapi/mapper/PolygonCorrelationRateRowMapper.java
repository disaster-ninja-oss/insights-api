package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class PolygonCorrelationRateRowMapper implements RowMapper<PolygonCorrelationRate> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public PolygonCorrelationRate mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return objectMapper.readValue(resultSet.getString(1), PolygonCorrelationRate.class);
    }
}
