package io.kontur.insightsapi.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    public static Double getNullableDouble(ResultSet argRs, String argColumnName) {
        try {
            return Optional.ofNullable(argRs.getBigDecimal(argColumnName)).map(BigDecimal::doubleValue).orElse(null);
        } catch (SQLException e) {
            logger.error("Can't get value from result set", e);
            return null;
        }
    }
}

