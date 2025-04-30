package io.kontur.insightsapi.repository;

import com.google.common.collect.Lists;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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

    public static Integer getIntValueByColumnName(ResultSet rs, String columnName) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException e) {
            logger.warn("Can't get int value from result set: {}", e.toString());
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

    public static List<String> getUUIDs(List<BivariateIndicatorDto> indicators) {
        List<String> uuids = Lists.newArrayList();
        for (BivariateIndicatorDto indicator : indicators) {
            uuids.add("'" + indicator.getInternalId() + "'");
        }
        return uuids;
    }

    public static List<String> getColumns(List<BivariateIndicatorDto> indicators) {
        List<String> columns = Lists.newArrayList();
        for (BivariateIndicatorDto indicator : indicators) {
            if (indicator.getId().equals("one")) {
                columns.add("1.0::float as \"one\"");
            } else if (indicator.getId().equals("area_km2")) {
                columns.add("ST_Area(h3_cell_to_boundary_geography(h3)) / 1000000.0 as \"area_km2\"");
            } else {
                columns.add(String.format("coalesce(avg(indicator_value) filter (where indicator_uuid = '%s'), 0) as \"%s\"",
                        indicator.getInternalId(), indicator.getId()));
            }
        }
        return columns;
    }

    public static String buildCTE(
            String resolution,
            List<BivariateIndicatorDto> bivariateIndicatorDtos,
            String additionalColumns) {
        List<String> columns = DatabaseUtil.getColumns(bivariateIndicatorDtos);
        List<String> uuids = DatabaseUtil.getUUIDs(bivariateIndicatorDtos);
        String sqlTemplate = """
                hexes as materialized (
                    select
                        distinct sh.h3
                    from stat_h3_geom sh, subdivision sb
                    where
                        sh.resolution = %s
                        and sh.geom && (select bbox from boxinput)
                        and ST_Intersects(sh.geom, sb.geom)
                ),
                res as (
                    select
                        h3, indicator_uuid, indicator_value
                    from hexes
                    join stat_h3_transposed st using(h3)
                    where indicator_uuid in (
                        -- list of required indicator uuids for this query:
                        %s
                )),
                indicators_as_columns as (
                    select
                        h3,
                        -- transpose res cte to column view:
                        %s
                        -- additionalColumns
                        %s
                    from res
                    group by h3
                )
        """;
        return String.format(
                sqlTemplate,
                resolution,
                StringUtils.join(uuids, ", "),
                StringUtils.join(columns, ", "),
                additionalColumns
        );
    }
}

