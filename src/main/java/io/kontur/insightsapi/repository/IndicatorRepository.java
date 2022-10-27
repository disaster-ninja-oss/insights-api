package io.kontur.insightsapi.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.FileUploadResultDto;
import io.kontur.insightsapi.exception.ConnectionException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(IndicatorRepository.class);
    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ObjectMapper objectMapper;

    private final DataSource dataSource;

    //TODO: temporary field, remove when we have final version of transposed stat_h3 table
    @Value("${database.transposed.table}")
    private String transposedTableName;

    @Value("${database.bivariate.indicators.table}")
    private String bivariateIndicatorsTableName;

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
        String bivariateIndicatorsQuery = String.format("INSERT INTO %s (param_id,param_label,copyrights,direction,is_base,param_uuid,owner,state,is_public,allowed_users,date) " +
                "VALUES (:id,:label,:copyrights::json,:direction::json,:isBase,gen_random_uuid(),null,'NEW',:isPublic,:allowedUsers,now()) RETURNING param_uuid;", bivariateIndicatorsTableName);
        return namedParameterJdbcTemplate.queryForObject(bivariateIndicatorsQuery, paramSource, String.class);
    }

    public FileUploadResultDto uploadCSVFileIntoTempTable(FileItemStream file) throws SQLException, IOException, ConnectionException {


        String tempTableName = generateTempTableName();

        String tempTableQuery = String.format("CREATE UNLOGGED TABLE %s (h3 h3index, value double precision)", tempTableName);
        jdbcTemplate.update(tempTableQuery);

        var copyManagerQuery = String.format("COPY %s FROM STDIN DELIMITER ','", tempTableName);

        long numberOfInsertedRows;

        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             InputStream fileInputStream = file.openStream()) {

            if (connection.isWrapperFor(Connection.class)) {

                CopyManager copyManager = new CopyManager((BaseConnection) connection.unwrap(Connection.class));
                numberOfInsertedRows = copyManager.copyIn(copyManagerQuery, fileInputStream);
                return new FileUploadResultDto(tempTableName, numberOfInsertedRows, null);
            } else {
                logger.error("Could not connect ot Copy Manager");
                throw new ConnectionException("Connection was closed unpredictably. Can not obtain connection for CopyManager");
            }
        } catch (Exception e) {
            return new FileUploadResultDto(null, 0, e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> copyDataToStatH3(FileUploadResultDto fileUploadResultDto, String uuid) {
        var copyDataFromTempToStatH3WithUuidQuery = String.format("INSERT INTO %s select h3, value, '%s' from %s", transposedTableName, uuid, fileUploadResultDto.getTempTableName());
        long numberOfCopiedRows = jdbcTemplate.update(copyDataFromTempToStatH3WithUuidQuery);

        var dropTempTableQuery = String.format("DROP TABLE %s", fileUploadResultDto.getTempTableName());
        jdbcTemplate.update(dropTempTableQuery);

        if (numberOfCopiedRows != fileUploadResultDto.getNumberOfUploadedRows()) {
            logger.warn(String.format("No errors during uploading occurred but records number validation did not pass: " +
                    "uploaded from CSV = %s, number of records put in database = %s, uuid = %s", fileUploadResultDto.getNumberOfUploadedRows(), numberOfCopiedRows, uuid));
            return ResponseEntity.ok().body(String.format("No errors during uploading occurred but records number validation did not pass: " +
                    "uploaded from CSV = %s, number of records put in database = %s, uuid = %s", fileUploadResultDto.getNumberOfUploadedRows(), numberOfCopiedRows, uuid));
        }

        return ResponseEntity.ok().body(uuid);
    }

    private String generateTempTableName() {
        return "_" + RandomStringUtils.randomAlphanumeric(29).toLowerCase();
    }

    @Transactional
    public void deleteIndicator(String uuid) {
        jdbcTemplate.update(String.format("DELETE FROM %s WHERE param_uuid = '%s'", bivariateIndicatorsTableName, uuid));
    }
}
