package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.Transformation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class TransformationRowMapper implements RowMapper<Transformation> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Transformation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return objectMapper.readValue(resultSet.getString(1), Transformation.class);
    }
}
