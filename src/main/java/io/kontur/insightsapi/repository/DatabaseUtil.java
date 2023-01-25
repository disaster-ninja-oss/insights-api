package io.kontur.insightsapi.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseUtil {

    public static final String ERROR_EMPTY_RESULT = "Empty result error for geometry %s";
    public static final String ERROR_TIMEOUT = "Connection to database issue or query timeout error for geometry %s";
    public static final String ERROR_SQL = "Sql exception for geometry %s";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    public static Double getNullableDouble(ResultSet argRs, String argColumnName) {
        try {
            return Optional.ofNullable(argRs.getBigDecimal(argColumnName)).map(BigDecimal::doubleValue).orElse(null);
        } catch (SQLException e) {
            logger.error("Can't get value from result set", e);
            return null;
        }
    }

    public static String getStringValueByColumnName(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            logger.error("Can't get value from result set", e);
            return null;
        }
    }
}

