package io.kontur.insightsapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

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
            int itemIndex = 0;

            while (itemIterator.hasNext()) {
                FileItemStream item = itemIterator.next();
                String name = item.getFieldName();

                if (!item.isFormField() && "file".equals(name) && itemIndex == 1) {

                    fileUploadResultDto = indicatorRepository.uploadCSVFileIntoTempTable(item);

                } else if ("parameters".equals(name) && itemIndex == 0) {

                    BivariateIndicatorDto bivariateIndicatorDto = parseRequestFormDataParameters(item);

                    Set<ConstraintViolation<BivariateIndicatorDto>> validationViolations = validateParameters(bivariateIndicatorDto);
                    if (validationViolations.isEmpty()) {
                        //TODO: create or update indicator
                        uuid = indicatorRepository.createIndicator(bivariateIndicatorDto);
                        itemIndex++;
                    } else {
                        StringBuilder validationErrorMessage = new StringBuilder();
                        for (ConstraintViolation<BivariateIndicatorDto> bivariateIndicatorDtoConstraintViolation : validationViolations) {
                            validationErrorMessage.append(bivariateIndicatorDtoConstraintViolation.getMessage()).append(". ");
                        }
                        return ResponseEntity.status(400).body(validationErrorMessage.toString());
                    }
                } else {
                    return ResponseEntity.status(400).body("Wrong field parameter or wrong parameters order in multipart request: " +
                            "please send a request with multipart data with keys 'parameters' and 'file' in a corresponding order.");
                }
            }

            if (Strings.isNotEmpty(uuid) && Strings.isNotEmpty(fileUploadResultDto.getTempTableName())) {
                return indicatorRepository.copyDataToStatH3(fileUploadResultDto, uuid);
            } else if (Strings.isNotEmpty(uuid) && Strings.isEmpty(fileUploadResultDto.getTempTableName())) {

                indicatorRepository.deleteIndicator(uuid);

                if (Strings.isNotEmpty(fileUploadResultDto.getErrorMessage())) {
                    logger.error(fileUploadResultDto.getErrorMessage());
                    return ResponseEntity.status(500).body(fileUploadResultDto.getErrorMessage());
                }
                logger.error("File was absent from request");
                return ResponseEntity.status(400).body("File was absent from request");
            } else {
                logger.error("Could not process request, neither indicator nor h3 indexes were created");
                return ResponseEntity.status(500).body("Could not process request, neither indicator nor h3 indexes were created");
            }

        } catch (MismatchedInputException exception) {
            String incorrectFieldMessage = String.format("%s field has an incorrect type: %s",
                    exception.getPath().get(0).getFieldName(),
                    exception.getMessage());
            logger.error(incorrectFieldMessage);
            return ResponseEntity.status(400).body(incorrectFieldMessage);
        } catch (FileUploadException | IOException exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.status(400).body(exception.getMessage());
        } catch (SQLException | ConnectionException exception) {
            logger.error(exception.getMessage());
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    private Set<ConstraintViolation<BivariateIndicatorDto>> validateParameters(BivariateIndicatorDto bivariateIndicatorDto) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(bivariateIndicatorDto);
        }
    }

    private BivariateIndicatorDto parseRequestFormDataParameters(FileItemStream item) throws IOException {
        return objectMapper.readValue(item.openStream(), BivariateIndicatorDto.class);
    }
}
