package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.exception.IndicatorDataProcessingException;
import io.kontur.insightsapi.repository.IndicatorRepository;
import io.kontur.insightsapi.service.auth.AuthService;
import lombok.AllArgsConstructor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.*;
import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@AllArgsConstructor
public class IndicatorService {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorService.class);

    private final IndicatorRepository indicatorRepository;

    private final ServletFileUpload upload;

    private final ObjectMapper objectMapper;

    private final AuthService authService;

    public static final int UUID_STRING_LENGTH = 36;

    public ResponseEntity<String> uploadIndicatorData(HttpServletRequest request, boolean isUpdate) {
        try {
            BivariateIndicatorDto indicatorMetadata = null;
            FileItemIterator itemIterator = upload.getItemIterator(request);
            int itemIndex = 0;

            while (itemIterator.hasNext()) {
                FileItemStream item = itemIterator.next();

                if ("parameters".equals(item.getFieldName()) && itemIndex == 0) {
                    indicatorMetadata = parseRequestFormDataParameters(item);
                    validateParameters(indicatorMetadata);

                    indicatorMetadata.setOwner(authService.getCurrentUsername().orElseThrow());

                    if (isUpdate) {
                        if (invalidIndicatorExternalId(indicatorMetadata.getExternalId())) {
                            return logAndReturnErrorWithMessage(HttpStatus.NOT_FOUND,
                                    "Indicator with uuid " + indicatorMetadata.getExternalId() + " not found");
                        }
                    } else {
                        indicatorMetadata.setExternalId(randomUUID().toString());
                    }
                    itemIndex++;
                } else if (!item.isFormField() && "file".equals(item.getFieldName()) && itemIndex == 1) {
                    indicatorRepository.uploadCsvFile(item, indicatorMetadata);
                    logger.info("Upload of csv file for indicator with uuid {} has been done successfully", indicatorMetadata.getExternalId());
                    return ResponseEntity.ok().body(indicatorMetadata.getExternalId());
                } else {
                    return logAndReturnErrorWithMessage(HttpStatus.BAD_REQUEST, "Wrong field parameter or " +
                            "wrong parameters order in multipart request: please send a request with multipart data " +
                            "with keys 'parameters' and 'file' in a corresponding order");
                }
            }
            return logAndReturnErrorWithMessage(HttpStatus.BAD_REQUEST,
                    "Could not process request, neither indicator nor h3 indexes were created");
        } catch (FileUploadException | IOException | ValidationException | IndicatorDataProcessingException e) {
            return logAndReturnErrorWithMessage(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (NoSuchElementException e) {
            return logAndReturnErrorWithMessage(HttpStatus.UNAUTHORIZED, "Incorrect authentication data: could not get username", e);
        } catch (Exception e) {
            return logAndReturnErrorWithMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Could not process request, neither indicator nor h3 indexes were created", e);
        }
    }

    public void updateIndicatorsLastUpdateDate(Instant lastUpdated) {
        indicatorRepository.updateIndicatorsLastUpdateDate(lastUpdated);
    }

    public Instant getIndicatorsLastUpdateDate() {
        return indicatorRepository.getIndicatorsLastUpdateDate();
    }

    public List<BivariateIndicatorDto> getAllIndicators() {
        return indicatorRepository.getAllIndicators();
    }

    public List<BivariateIndicatorDto> getGeneralIndicators() {
        return indicatorRepository.getGeneralIndicators();
    }

    public boolean invalidIndicatorExternalId(String externalId) {
        return isEmpty(externalId)
                || externalId.length() != UUID_STRING_LENGTH
                || indicatorRepository.getIndicatorsByExternalId(externalId).isEmpty();
    }

    private void validateParameters(BivariateIndicatorDto bivariateIndicatorDto) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<BivariateIndicatorDto>> validationViolations =
                    validator.validate(bivariateIndicatorDto);
            if (!validationViolations.isEmpty()) {
                StringBuilder validationErrorMessage = new StringBuilder();
                for (ConstraintViolation<BivariateIndicatorDto> bivariateIndicatorDtoConstraintViolation :
                        validationViolations) {
                    validationErrorMessage.append(bivariateIndicatorDtoConstraintViolation.getMessage()).append(". ");
                }
                throw new ValidationException(validationErrorMessage.toString());
            }
        }
    }

    private BivariateIndicatorDto parseRequestFormDataParameters(FileItemStream item) throws IOException {
        try {
            return objectMapper.readValue(item.openStream(), BivariateIndicatorDto.class);
        } catch (JsonParseException exception) {
            throw new IOException(generateExceptionMessage(exception.getProcessor().getParsingContext()
                    .getCurrentName()));
        } catch (MismatchedInputException exception) {
            throw new IOException(generateExceptionMessage(exception.getPath().get(0).getFieldName()));
        }
    }

    private String generateExceptionMessage(String fieldName) {
        if (fieldName == null) {
            return "Incorrect parameters json";
        }
        return switch (fieldName) {
            case "isPublic", "isBase" -> String.format("%s field supports only boolean values", fieldName);
            case "id", "label" -> String.format("%s field supports only string values", fieldName);
            case "copyrights", "allowedUsers" ->
                    String.format("Incorrect type of %s field, array expected.", fieldName);
            case "direction" -> String.format("Incorrect type of %s field, array of arrays expected.", fieldName);
            default -> String.format("Incorrect type of %s field", fieldName);
        };
    }

    private ResponseEntity<String> logAndReturnErrorWithMessage(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }

    private ResponseEntity<String> logAndReturnErrorWithMessage(HttpStatus status, String message, Exception e) {
        logger.error(message, e);
        return ResponseEntity.status(status).body(message);
    }
}
