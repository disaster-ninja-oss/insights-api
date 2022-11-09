package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.FileUploadResultDto;
import io.kontur.insightsapi.exception.ConnectionException;
import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

@Service
@AllArgsConstructor
public class IndicatorService {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorService.class);

    private final IndicatorRepository indicatorRepository;

    private final ServletFileUpload upload;

    private final ObjectMapper objectMapper;

    public ResponseEntity<String> uploadIndicatorData(HttpServletRequest request) {
        try {

            FileItemIterator itemIterator = upload.getItemIterator(request);
            FileUploadResultDto fileUploadResultDto = new FileUploadResultDto();
            String uuid = "";

            while (itemIterator.hasNext()) {
                FileItemStream item = itemIterator.next();
                if (!item.isFormField()) {
                    fileUploadResultDto = indicatorRepository.uploadCSVFileIntoTempTable(item);
                } else {
                    uuid = indicatorRepository.createIndicator(parseRequestFormDataParameters(item));
                }
            }

            if (Strings.isNotEmpty(uuid) && Strings.isNotEmpty(fileUploadResultDto.getTempTableName())) {
                return indicatorRepository.copyDataToStatH3(fileUploadResultDto, uuid);
            } else {
                logger.warn("Either file or parameters were absent from request");
                return ResponseEntity.status(400).body("Either file or parameters were absent from request");
            }

        } catch (FileUploadException | IOException exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.status(400).body(exception.getMessage());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    private BivariateIndicatorDto parseRequestFormDataParameters(FileItemStream item) throws IOException {
        try {
            return objectMapper.readValue(item.openStream(), BivariateIndicatorDto.class);
        } catch (JsonParseException exception) {
            throw new IOException(generateExceptionMessage(exception.getProcessor().getParsingContext().getCurrentName()));
        } catch (MismatchedInputException exception) {
            throw new IOException(generateExceptionMessage(exception.getPath().get(0).getFieldName()));
        }
    }

    private String generateExceptionMessage(String fieldName) {
        return switch (fieldName) {
            case "isPublic", "isBase" -> String.format("%s field supports only boolean values", fieldName);
            case "id", "label" -> String.format("%s field supports only string values", fieldName);
            case "copyrights", "allowedUsers" -> String.format("Incorrect type of %s field, array expected.", fieldName);
            case "direction" -> String.format("Incorrect type of %s field, array of arrays expected.", fieldName);
            default -> String.format("Incorrect type of %s field", fieldName);
        };
    }
}
