package io.kontur.insightsapi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.BivariateStatistic;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Service
@RequiredArgsConstructor
public class BivariateStatisticRowMapper implements RowMapper<BivariateStatistic> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public BivariateStatistic mapRow(ResultSet resultSet, int i) {
        var t = objectMapper.readValue(resultSet.getString(1), BivariateStatistic.class);
        System.out.println(1);
        return t;
    }
}
