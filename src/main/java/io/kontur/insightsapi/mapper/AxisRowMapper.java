package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.Axis;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class AxisRowMapper implements RowMapper<Axis> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Axis mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return objectMapper.readValue(resultSet.getString(1), Axis.class);
    }
}
