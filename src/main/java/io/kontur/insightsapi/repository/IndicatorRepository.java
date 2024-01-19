package io.kontur.insightsapi.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.exception.IndicatorDataProcessingException;
import io.kontur.insightsapi.mapper.BivariateIndicatorRowMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItemStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Repository
@RequiredArgsConstructor
public class IndicatorRepository {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    private final DataSource dataSource;

    @Value("classpath:/sql.queries/insert_bivariate_indicators.sql")
    private Resource insertBivariateIndicators;

    private final QueryFactory queryFactory;

    private final BivariateIndicatorRowMapper bivariateIndicatorRowMapper;

    @Value("${calculations.bivariate.transposed.table}")
    private String transposedTableName;

    @Value("${calculations.bivariate.indicators.test.table}")
    private String bivariateIndicatorsMetadataTableName;

    @Value("${calculations.bivariate.indicators.table}")
    private String bivariateIndicatorsTableName;

    @Value("${calculations.useStatSeparateTables:false}")
    private Boolean useStatSeparateTables;

    private final ThreadPoolExecutor uploadExecutor;

    public void uploadCsvFile(FileItemStream file, BivariateIndicatorDto bivariateIndicatorDto) throws IndicatorDataProcessingException {
        Connection connection = null;

        try (PipedInputStream pipedInputStream = new PipedInputStream();
             PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {

            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            String internalId = createIndicator(connection, bivariateIndicatorDto);

            Future<?> uploadTask = submitUploadTask(file, internalId, pipedOutputStream);

            copyFile(connection, pipedInputStream);

            try {
                uploadTask.get();
            } catch (Exception e) {
                throw new IndicatorDataProcessingException(e.getCause().getMessage(), e);
            }
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception e1) {
                    throw new IndicatorDataProcessingException("Failed to rollback indicator upload transaction", e);
                }
            }
            throw new IndicatorDataProcessingException(String.format("Failed to copy indicator. %s", e.getMessage()), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception e1) {
                    logger.error("Failed to reset auto-commit behavior after indicator upload", e1);
                }
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    public String createIndicator(Connection connection, BivariateIndicatorDto bivariateIndicatorDto) throws JsonProcessingException, SQLException {
        String insertQuery = String.format(queryFactory.getSql(insertBivariateIndicators), bivariateIndicatorsMetadataTableName);

        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            initParams(ps, bivariateIndicatorDto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("internal_id", String.class);
                } else {
                    throw new SQLException("Failed to retrieve internal_id after insert");
                }
            }
        }
    }

    private void copyFile(Connection connection, InputStream inputStream) throws SQLException, IOException {
        if (connection.isWrapperFor(Connection.class)) {
            CopyManager copyManager = new CopyManager((BaseConnection) connection.unwrap(Connection.class));
            copyManager.copyIn(String.format("COPY %s FROM STDIN DELIMITER ',' null 'NULL'", transposedTableName), inputStream);
        } else {
            logger.error("Could not connect to Copy Manager");
            throw new IndicatorDataProcessingException("Connection was closed unpredictably. Can not obtain connection for CopyManager");
        }
    }

    private Future<?> submitUploadTask(FileItemStream file, String internalId, PipedOutputStream pipedOutputStream) {
        return uploadExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pipedOutputStream, StandardCharsets.UTF_8))) {
                String row;
                while ((row = reader.readLine()) != null) {
                    String[] rowValues = row.split(",");
                    writer.write(String.join(",", rowValues[0], internalId, rowValues[1]));
                    writer.newLine();
                }
            } catch (Exception e) {
                throw new IndicatorDataProcessingException("Failed to adjust incoming csv stream with uuid", e);
            }
        });
    }

    public List<BivariateIndicatorDto> getIndicatorsByExternalId(String externalId) {
        return jdbcTemplate.query(
                String.format("SELECT * FROM %s WHERE external_id = '%s'::uuid",
                        bivariateIndicatorsMetadataTableName,
                        externalId),
                bivariateIndicatorRowMapper);
    }

    public List<BivariateIndicatorDto> getIndicatorsByOwner(String owner) {
        return jdbcTemplate.query(
                "SELECT * FROM " + bivariateIndicatorsMetadataTableName + " WHERE owner = ?",
                bivariateIndicatorRowMapper, owner);
    }

    public List<BivariateIndicatorDto> getIndicatorsByOwnerAndParamId(String owner, String paramId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + bivariateIndicatorsMetadataTableName + " WHERE owner = ? AND param_id = ?",
                bivariateIndicatorRowMapper, owner, paramId);
    }

    public BivariateIndicatorDto getIndicatorByOwnerAndExternalId(String owner, String externalId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM " + bivariateIndicatorsMetadataTableName + " WHERE owner = ? AND external_id = ?::uuid",
                    bivariateIndicatorRowMapper, owner, externalId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // TODO: possibly will be added something about owner field here
    @Transactional(readOnly = true)
    public List<BivariateIndicatorDto> getAllBivariateIndicators() {
        return jdbcTemplate.query(String.format("SELECT * FROM %s", bivariateIndicatorsMetadataTableName),
                bivariateIndicatorRowMapper);
    }

    @Transactional(readOnly = true)
    public List<BivariateIndicatorDto> getSelectedBivariateIndicators(List<String> indicatorIds) {
        return jdbcTemplate.query(String.format("SELECT * FROM %s WHERE param_id in ('%s')",
                        bivariateIndicatorsMetadataTableName, String.join("','", indicatorIds)),
                bivariateIndicatorRowMapper);
    }

    //TODO: remove after transition from param_id to uuid as an identifier for indicator. Use 'getIndicatorByUuid' method in future instead
    @Deprecated
    public String getLabelByParamId(String paramId) {
        String bivariateIndicatorsTable = useStatSeparateTables ? bivariateIndicatorsMetadataTableName
                : bivariateIndicatorsTableName;
        return jdbcTemplate.queryForObject(String.format("SELECT param_label FROM %s where param_id = '%s'",
                bivariateIndicatorsTable, paramId), String.class);
    }

    public void updateIndicatorsLastUpdateDate(Instant lastUpdated) {
        jdbcTemplate.update(String.format("UPDATE %s SET last_updated = '%s'", bivariateIndicatorsMetadataTableName,
                Timestamp.from(lastUpdated)));
    }

    public Instant getIndicatorsLastUpdateDate() {
        Timestamp lastUpdated = jdbcTemplate.queryForObject(String.format("SELECT MAX(last_updated) FROM %s",
                bivariateIndicatorsMetadataTableName), Timestamp.class);
        return lastUpdated != null ? lastUpdated.toInstant() : null;
    }

    private void initParams(PreparedStatement ps, BivariateIndicatorDto bivariateIndicatorDto) throws SQLException, JsonProcessingException {
        ps.setString(1, bivariateIndicatorDto.getId());
        ps.setString(2, bivariateIndicatorDto.getLabel());
        ps.setString(3, bivariateIndicatorDto.getCopyrights() == null ? null : objectMapper.writeValueAsString(bivariateIndicatorDto.getCopyrights()));
        ps.setString(4, bivariateIndicatorDto.getDirection() == null ? null : objectMapper.writeValueAsString(bivariateIndicatorDto.getDirection()));
        ps.setBoolean(5, bivariateIndicatorDto.getIsBase());
        ps.setString(6, bivariateIndicatorDto.getExternalId());
        ps.setString(7, bivariateIndicatorDto.getOwner());
        ps.setBoolean(8, bivariateIndicatorDto.getIsPublic());
        ps.setString(9, bivariateIndicatorDto.getAllowedUsers() == null ? null : objectMapper.writeValueAsString(bivariateIndicatorDto.getAllowedUsers()));
        ps.setString(10, bivariateIndicatorDto.getDescription());
        ps.setString(11, bivariateIndicatorDto.getCoverage());
        //TODO: discuss these values, should be some default values if not specified
        ps.setString(12, bivariateIndicatorDto.getUpdateFrequency());
        ps.setString(13, bivariateIndicatorDto.getApplication() == null ? null : objectMapper.writeValueAsString(bivariateIndicatorDto.getApplication()));
        ps.setString(14, bivariateIndicatorDto.getUnitId());
        ps.setString(15, bivariateIndicatorDto.getLastUpdated() == null ? null : bivariateIndicatorDto.getLastUpdated().toString());
    }
}
