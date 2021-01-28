package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CorrelationRateResolver implements GraphQLResolver<PolygonStatistic> {

    private final StatisticRepository statisticRepository;

    private final GeometryTransformer geometryTransformer;

    public List<PolygonCorrelationRate> getCorrelationRates(PolygonStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getArguments().get("polygonStatisticRequest");
        if (!arguments.containsKey("polygon")) {
            return statisticRepository.getAllCorrelationRateStatistics();
        }
        var transformedGeometry = geometryTransformer.transform((String) arguments.get("polygon"));
        if (!arguments.keySet().containsAll(List.of("xNumeratorList", "yNumeratorList"))) {
            List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos = statisticRepository.getNumeratorsDenominatorsForCorrelation();
            return Lists.partition(numeratorsDenominatorsDtos, 500).parallelStream()
                    .map(sourceDtoList -> {
                        List<PolygonCorrelationRate> result = new ArrayList<>();
                        List<Double> correlations = statisticRepository.getPolygonCorrelationRateStatisticsBatch(sourceDtoList, transformedGeometry);
                        for (int i = 0; i < sourceDtoList.size(); i++) {
                            PolygonCorrelationRate polygonCorrelationRate = new PolygonCorrelationRate();
                            polygonCorrelationRate.setX(createAxis(sourceDtoList.get(i).getXLabel(),
                                    sourceDtoList.get(i).getXNumerator(), sourceDtoList.get(i).getXDenominator()));
                            polygonCorrelationRate.setY(createAxis(sourceDtoList.get(i).getYLabel(),
                                    sourceDtoList.get(i).getYNumerator(), sourceDtoList.get(i).getYDenominator()));
                            polygonCorrelationRate.setQuality(sourceDtoList.get(i).getQuality());
                            polygonCorrelationRate.setCorrelation(correlations.get(i));
                            polygonCorrelationRate.setRate(correlations.get(i));
                            result.add(polygonCorrelationRate);
                        }
                        return result;
                    })
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

    private Axis createAxis(String label, String num, String den) {
        Axis axis = new Axis();
        axis.setLabel(label);
        axis.setQuotient(List.of(num, den));
        return axis;
    }

    private Comparator<PolygonCorrelationRate> correlationRateComparator() {
        return Comparator.nullsLast(Comparator.<PolygonCorrelationRate, Double>comparing(c -> Math.abs(c.getCorrelation()) * c.getQuality()))
                .thenComparing(Comparator.<PolygonCorrelationRate, Double>comparing(c -> Math.abs(c.getCorrelation())).reversed());
    }
}
