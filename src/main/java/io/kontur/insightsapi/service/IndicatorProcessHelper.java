package io.kontur.insightsapi.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@AllArgsConstructor
public class IndicatorProcessHelper {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorProcessHelper.class);

    private final IndicatorService indicatorService;

    public static final int UUID_STRING_LENGTH = 36;

    public ResponseEntity<String> processIndicator(HttpServletRequest request, boolean isUpdate) {

        long uploadStartTime = System.currentTimeMillis();

        final ResponseEntity<String> response = indicatorService.uploadIndicatorData(request, isUpdate);

        long uploadTimeInSeconds = (System.currentTimeMillis() - uploadStartTime) / 1000;

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                && response.getBody().length() == UUID_STRING_LENGTH) {
            String uuid = response.getBody();

            logger.info("Upload of csv file for indicator with uuid {} has been done successfully and took {}", uuid,
                    String.format("%02d hours %02d minutes %02d seconds", uploadTimeInSeconds / 3600,
                            (uploadTimeInSeconds % 3600) / 60, (uploadTimeInSeconds % 60)));
        }

        return response;
    }
}
