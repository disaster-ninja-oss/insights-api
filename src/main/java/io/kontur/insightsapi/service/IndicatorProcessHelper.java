package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@AllArgsConstructor
public class IndicatorProcessHelper {

    private final IndicatorService indicatorService;

    private final AxisService axisService;

    private final IndicatorRepository indicatorRepository;

    private static final int UUID_STRING_LENGTH = 36;

    public ResponseEntity<String> processIndicator(HttpServletRequest request) {

        ResponseEntity<String> response = indicatorService.uploadIndicatorData(request);

//        TODO: run this part async in future
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody() && response.getBody().length() >= UUID_STRING_LENGTH) {
            List<BivariateIndicatorDto> incomingBivariateIndicatorDtoAsList =
                    List.of(indicatorRepository.getIndicatorByUuid(response.getBody().substring(response.getBody().length() - UUID_STRING_LENGTH)));

            axisService.createAxis(incomingBivariateIndicatorDtoAsList);
        }

        return response;
    }
}
