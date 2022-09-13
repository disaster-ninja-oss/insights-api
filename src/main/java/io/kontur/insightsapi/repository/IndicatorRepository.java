package io.kontur.insightsapi.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItemStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class IndicatorRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ObjectMapper objectMapper;

    @Transactional
    public String createIndicator(BivariateIndicatorDto bivariateIndicatorDto) throws JsonProcessingException {

        var paramSource = new MapSqlParameterSource()
                .addValue("id", bivariateIndicatorDto.getId())
                .addValue("label", bivariateIndicatorDto.getLabel())
                .addValue("copyrights", objectMapper.writeValueAsString(bivariateIndicatorDto.getCopyrights()))
                .addValue("direction", objectMapper.writeValueAsString(bivariateIndicatorDto.getDirection()))
                .addValue("isBase", bivariateIndicatorDto.getIsBase())
                .addValue("isPublic", bivariateIndicatorDto.getIsPublic())
                .addValue("allowedUsers", objectMapper.writeValueAsString(bivariateIndicatorDto.getAllowedUsers()));

        //TODO:add owner in future
        String queryBivariateIndicators = "INSERT INTO bivariate_indicators (param_id,param_label,copyrights,direction,is_base,param_uuid,owner,state,is_public,allowed_users,date) " +
                "VALUES (:id,:label,:copyrights::json,:direction::json,:isBase,gen_random_uuid(),null,'NEW',:isPublic,:allowedUsers,now()) RETURNING param_uuid;";
        return namedParameterJdbcTemplate.queryForObject(queryBivariateIndicators, paramSource, String.class);
    }

    @Transactional
    public ResponseEntity<String> addIndicatorData(FileItemStream file, String uuid) throws SQLException, IOException {

        String tempTableName = generateTempTableName(uuid);

        String tempTableQuery = "CREATE TEMPORARY TABLE " + tempTableName + " ON COMMIT DROP AS SELECT h3, value from stat_h3_test WITH NO DATA";
        jdbcTemplate.update(tempTableQuery);

        var queryCopyManager = "COPY " + tempTableName + " FROM STDIN DELIMITER ','";
        DataSource dataSource = jdbcTemplate.getDataSource();
        long numberOfInsertedRows;

        if (dataSource != null && DataSourceUtils.getConnection(dataSource).isWrapperFor(Connection.class)) {
            try (InputStream fileInputStream = file.openStream()) {

                CopyManager copyManager = new CopyManager((BaseConnection) DataSourceUtils.getConnection(jdbcTemplate.getDataSource()).unwrap(Connection.class));
                numberOfInsertedRows = copyManager.copyIn(queryCopyManager, fileInputStream);
            }
        } else {
            throw new SQLException("Connection was closed unpredictably");
        }

        var copyDataFromTempToStatH3WithUUID = "insert into stat_h3_test select h3, value, '" + uuid + "' from " + tempTableName;
        long numberOfCopiedRows = jdbcTemplate.update(copyDataFromTempToStatH3WithUUID);

        if (numberOfCopiedRows != numberOfInsertedRows) {
            return ResponseEntity.ok().body("No errors during uploading but rows number validation did not pass, check if all rows were uploaded.");
        }

        return ResponseEntity.ok().body(Long.toString(numberOfInsertedRows));
    }

    private String generateTempTableName(String uuid) {
        return "_".concat(uuid.replaceAll("-", "").substring(0, 30));
    }
}
