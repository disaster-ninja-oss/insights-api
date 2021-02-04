package io.kontur.insightsapi.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrelationRateRowMapper implements RowMapper<List<Double>> {

    @Override
    public List<Double> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            result.add(resultSet.getDouble(i+1));
        }
        return result;
    }
}
