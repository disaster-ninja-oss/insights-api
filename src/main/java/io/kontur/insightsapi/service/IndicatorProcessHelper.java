package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.IndicatorState;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@AllArgsConstructor
public class IndicatorProcessHelper {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorProcessHelper.class);

    private final IndicatorService indicatorService;

    private final AxisService axisService;

    private static final int UUID_STRING_LENGTH = 36;

    private final ThreadPoolExecutor calculationExecutor;

    public ResponseEntity<String> processIndicator(HttpServletRequest request) {

        long uploadStartTime = System.currentTimeMillis();

        final ResponseEntity<String> response = indicatorService.uploadIndicatorData(request);

        long uploadEndTime = System.currentTimeMillis();
        long uploadTimeInSeconds = (uploadEndTime - uploadStartTime) / 1000;

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                && response.getBody().length() == UUID_STRING_LENGTH) {
            String uuid = response.getBody();

            logger.info("Upload of csv file for indicator with uuid {} has been done successfully and took {}", uuid,
                    String.format("%02d hours %02d minutes %02d seconds", uploadTimeInSeconds / 3600,
                            (uploadTimeInSeconds % 3600) / 60, (uploadTimeInSeconds % 60)));

            calculationExecutor.submit(() -> {
                List<BivariateIndicatorDto> incomingBivariateIndicatorDtoAsList =
                        List.of(indicatorService.getIndicatorByUuid(uuid));

                indicatorService.updateIndicatorState(uuid, IndicatorState.CALCULATING);

                logger.info("Start calculations for indicator with uuid {}", uuid);
                long calculationStartTime = System.currentTimeMillis();

                axisService.createAxis(incomingBivariateIndicatorDtoAsList);

                long calculationEndTime = System.currentTimeMillis();
                long calculationTimeInSeconds = (calculationEndTime - calculationStartTime) / 1000;
                logger.info("Calculations for indicator with uuid {} have been done successfully and took {}", uuid,
                        String.format("%02d hours %02d minutes %02d seconds", calculationTimeInSeconds / 3600,
                                (calculationTimeInSeconds % 3600) / 60, (calculationTimeInSeconds % 60)));
                indicatorService.updateIndicatorState(uuid, IndicatorState.READY);
            });
        }
        logger.info("Current queue size with indicators to process: {}", calculationExecutor.getQueue().size());

        return response;
    }
}
