package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CorrelationRateResolver implements GraphQLResolver<BivariateStatistic> {

    private final StatisticRepository statisticRepository;

    private final GeometryTransformer geometryTransformer;

    public List<PolygonCorrelationRate> getCorrelationRates(BivariateStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getParent().getArguments().get("polygonStatisticRequest");
        if (!arguments.containsKey("polygon") && !arguments.containsKey("polygonV2")) {
            return statisticRepository.getAllCorrelationRateStatistics();
        }
        var transformedGeometry = getPolygon(arguments);
        if (!arguments.keySet().containsAll(List.of("xNumeratorList", "yNumeratorList"))) {
            List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos = statisticRepository.getNumeratorsDenominatorsForCorrelation();
            String finalTransformedGeometry = transformedGeometry;
            return Lists.partition(numeratorsDenominatorsDtos, 500).parallelStream()
                    .map(sourceDtoList -> calculatePolygonCorrelations(sourceDtoList, finalTransformedGeometry))
                    .flatMap(Collection::stream)
                    .sorted(correlationRateComparator())
                    .collect(Collectors.toList());
        }
        return statisticRepository.getPolygonNumeratorsCorrelationRateStatistics(PolygonStatisticRequest.builder()
                .polygon(transformedGeometry)
                .xNumeratorList((List<String>) arguments.get("xNumeratorList"))
                .yNumeratorList((List<String>) arguments.get("yNumeratorList"))
                .build());
    }

    private Comparator<PolygonCorrelationRate> correlationRateComparator() {
        return Comparator.nullsLast(Comparator.<PolygonCorrelationRate, Double>comparing(c -> Math.abs(c.getCorrelation()) * c.getQuality()).reversed())
                .thenComparing(Comparator.<PolygonCorrelationRate, Double>comparing(c -> Math.abs(c.getCorrelation())).reversed());
    }

    private List<PolygonCorrelationRate> calculatePolygonCorrelations(List<NumeratorsDenominatorsDto> sourceDtoList,
                                                                      String transformedGeometry) {
        List<PolygonCorrelationRate> result = new ArrayList<>();
        List<Double> correlations = statisticRepository.getPolygonCorrelationRateStatisticsBatch(sourceDtoList, transformedGeometry);
        for (int i = 0; i < sourceDtoList.size(); i++) {
            PolygonCorrelationRate polygonCorrelationRate = new PolygonCorrelationRate();
            polygonCorrelationRate.setX(Axis.builder()
                    .label(sourceDtoList.get(i).getXLabel())
                    .quotient(List.of(sourceDtoList.get(i).getXNumerator(), sourceDtoList.get(i).getXDenominator()))
                    .build());
            polygonCorrelationRate.setY(Axis.builder()
                    .label(sourceDtoList.get(i).getYLabel())
                    .quotient(List.of(sourceDtoList.get(i).getYNumerator(), sourceDtoList.get(i).getYDenominator()))
                    .build());
            polygonCorrelationRate.setQuality(sourceDtoList.get(i).getQuality());
            polygonCorrelationRate.setCorrelation(correlations.get(i));
            polygonCorrelationRate.setRate(correlations.get(i));
            result.add(polygonCorrelationRate);
        }
        return result;
    }

    private String getPolygon(Map<String, Object> arguments) throws JsonProcessingException {
        if (arguments.containsKey("polygon")) {
            return geometryTransformer.transform(arguments.get("polygon").toString());
        }
        if (arguments.containsKey("polygonV2")) {
            return geometryTransformer.transform(arguments.get("polygonV2").toString());
        }
        return null;
    }
}
