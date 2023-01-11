package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AxisService {

    private static final Logger logger = LoggerFactory.getLogger(AxisService.class);

    private final IndicatorRepository indicatorRepository;

    public void createAxis(@NotNull List<BivariateIndicatorDto> indicatorsForAxis) {

        List<BivariativeAxisDto> axisForCurrentIndicators = new ArrayList<>();

        for (BivariateIndicatorDto indicatorForAxis : indicatorsForAxis) {

            List<BivariateIndicatorDto> allIndicatorsExceptIndicatorForAxis = indicatorRepository
                    .getAllBivariateIndicators()
                    .stream()
                    .filter(bivariateIndicatorDto -> !bivariateIndicatorDto.getUuid().equals(indicatorForAxis.getUuid()))
                    .toList();

            if (indicatorForAxis.getIsBase()) {
                axisForCurrentIndicators.addAll(allIndicatorsExceptIndicatorForAxis
                        .stream()
                        .map(bivariateIndicatorDto -> new BivariativeAxisDto(bivariateIndicatorDto.getId(), indicatorForAxis.getId()))
                        .toList());
            }

            axisForCurrentIndicators.addAll(allIndicatorsExceptIndicatorForAxis
                    .stream()
                    .filter(BivariateIndicatorDto::getIsBase)
                    .filter(bivariateIndicatorDto -> !indicatorsForAxis.stream().map(BivariateIndicatorDto::getUuid).toList().contains(bivariateIndicatorDto.getUuid()))
                    .map(bivariateIndicatorDto -> new BivariativeAxisDto(indicatorForAxis.getId(), bivariateIndicatorDto.getId()))
                    .toList());
        }
    }
}
