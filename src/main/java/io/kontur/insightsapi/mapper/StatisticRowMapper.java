package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.Statistic;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Service
@RequiredArgsConstructor
public class StatisticRowMapper implements RowMapper<Statistic> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Statistic mapRow(ResultSet resultSet, int i) {
        return objectMapper.readValue(resultSet.getString(1), Statistic.class);
    }
}
