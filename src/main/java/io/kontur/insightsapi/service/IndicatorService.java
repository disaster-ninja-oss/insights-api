package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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

    public ResponseEntity<String> uploadIndicatorData(String uuid, HttpServletRequest request) {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator itemIterator = upload.getItemIterator(request);

            while (itemIterator.hasNext()) {
                FileItemStream item = itemIterator.next();
                if (!item.isFormField()) {
                    return indicatorRepository.addIndicatorData(item, uuid);
                }
            }

            return ResponseEntity.status(400).body("No file found in request");

        } catch (FileUploadException | IOException exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.status(400).body(exception.getMessage());
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            return ResponseEntity.status(500).body("Database usage issues");
        }
    }

    public String createIndicator(BivariateIndicatorDto bivariateIndicatorDto) throws JsonProcessingException {
        return indicatorRepository.createIndicator(bivariateIndicatorDto);
    }
}
