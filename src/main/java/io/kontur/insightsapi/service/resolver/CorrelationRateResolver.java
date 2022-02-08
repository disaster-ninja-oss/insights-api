package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.ForAvgCorrelationDto;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.dto.PureNumeratorDenominatorDto;
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
        //no polygons defined
        if (!arguments.containsKey("polygon") && !arguments.containsKey("polygonV2")) {
            var correlationRateList = statisticRepository.getAllCorrelationRateStatistics();
            fillParent(correlationRateList);
            return correlationRateList;
        }
        var transformedGeometry = getPolygon(arguments);
        if (!arguments.keySet().containsAll(List.of("xNumeratorList", "yNumeratorList"))) {

            //get numr & denm from bivariate_axis & bivariate_indicators size nearly 17k
            List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos = statisticRepository.getNumeratorsDenominatorsForCorrelation();
            String finalTransformedGeometry = transformedGeometry;

            //calculate correlation for every bivariate_axis in defined polygon that intersects h3
            var correlationRateList = Lists.partition(numeratorsDenominatorsDtos, 500).parallelStream()
                    .map(sourceDtoList -> calculatePolygonCorrelations(sourceDtoList, finalTransformedGeometry))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            //calculate avg correlation for xy
            var avgCorrelationXY = calculateAvgCorrelationXY(correlationRateList);

            //set avg correlation rate
            fillAvgCorrelation(correlationRateList, avgCorrelationXY);

            //should be sorted with correlationRateComparator
            correlationRateList.sort(correlationRateComparator());

            fillParent(correlationRateList);
            return correlationRateList;
        }
        return statisticRepository.getPolygonNumeratorsCorrelationRateStatistics(PolygonStatisticRequest.builder()
                .polygon(transformedGeometry)
                .xNumeratorList((List<String>) arguments.get("xNumeratorList"))
                .yNumeratorList((List<String>) arguments.get("yNumeratorList"))
                .build());
    }

    private void fillParent(List<PolygonCorrelationRate> correlationRateList) {
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > 0.8) {
                if (polygonCorrelationRate.getAvgCorrelationX() > polygonCorrelationRate.getAvgCorrelationY()) {
                    polygonCorrelationRate.getX().setParent(polygonCorrelationRate.getY().getQuotient());
                }
                if (polygonCorrelationRate.getAvgCorrelationY() > polygonCorrelationRate.getAvgCorrelationX()) {
                    polygonCorrelationRate.getY().setParent(polygonCorrelationRate.getX().getQuotient());
                }
            }
        }
    }

    private Comparator<PolygonCorrelationRate> correlationRateComparator() {
        return Comparator.<PolygonCorrelationRate, Double>comparing(c -> c.getAvgCorrelationX() * c.getAvgCorrelationY()).reversed();
    }

    private List<PolygonCorrelationRate> calculatePolygonCorrelations(List<NumeratorsDenominatorsDto> sourceDtoList,
                                                                      String transformedGeometry) {
        List<PolygonCorrelationRate> result = new ArrayList<>();

        //run for every 500 bivariative_axis sourceDtoList size = 500 & get correlationList
        List<Double> correlations = statisticRepository.getPolygonCorrelationRateStatisticsBatch(sourceDtoList, transformedGeometry);
        for (int i = 0; i < sourceDtoList.size(); i++) {
            PolygonCorrelationRate polygonCorrelationRate = new PolygonCorrelationRate();

            ///combine them on Axis with quality, correlation & rate
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

    private void calculateDataForAvgCorrelation(PureNumeratorDenominatorDto numeratorDenominator,
                                                PolygonCorrelationRate currentRate,
                                                Map<PureNumeratorDenominatorDto, ForAvgCorrelationDto> result) {
        if (!result.containsKey(numeratorDenominator)) {
            result.put(numeratorDenominator, new ForAvgCorrelationDto(0.0, 0));
        }
        var forAvgCorrelationDto = result.get(numeratorDenominator);
        forAvgCorrelationDto.setSum(forAvgCorrelationDto.getSum() + Math.abs(currentRate.getCorrelation()));
        forAvgCorrelationDto.setNumber(forAvgCorrelationDto.getNumber() + 1);
    }

    private List<Map<PureNumeratorDenominatorDto, Double>> calculateAvgCorrelationXY(List<PolygonCorrelationRate> correlationRateList) {
        Map<PureNumeratorDenominatorDto, ForAvgCorrelationDto> xMap = new HashMap<>();
        Map<PureNumeratorDenominatorDto, ForAvgCorrelationDto> yMap = new HashMap<>();
        for (PolygonCorrelationRate currentRate : correlationRateList) {
            var xNum = currentRate.getX().getQuotient().get(0);
            var xDen = currentRate.getX().getQuotient().get(1);
            var yNum = currentRate.getY().getQuotient().get(0);
            var yDen = currentRate.getY().getQuotient().get(1);
            var numeratorDenominatorX = new PureNumeratorDenominatorDto(xNum, xDen);
            var numeratorDenominatorY = new PureNumeratorDenominatorDto(yNum, yDen);

            calculateDataForAvgCorrelation(numeratorDenominatorX, currentRate, xMap);
            calculateDataForAvgCorrelation(numeratorDenominatorY, currentRate, yMap);
        }

        Map<PureNumeratorDenominatorDto, Double> xMapResult = xMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getSum() / entry.getValue().getNumber()));

        Map<PureNumeratorDenominatorDto, Double> yMapResult = yMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getSum() / entry.getValue().getNumber()));

        return List.of(xMapResult, yMapResult);
    }

    private void fillAvgCorrelation(List<PolygonCorrelationRate> correlationRateList,
                                    List<Map<PureNumeratorDenominatorDto, Double>> avgCorrelationList) {
        var xAvgCorrelationMap = avgCorrelationList.get(0);
        var yAvgCorrelationMap = avgCorrelationList.get(1);
        for (PolygonCorrelationRate currentRate : correlationRateList) {
            var xNum = currentRate.getX().getQuotient().get(0);
            var xDen = currentRate.getX().getQuotient().get(1);
            var yNum = currentRate.getY().getQuotient().get(0);
            var yDen = currentRate.getY().getQuotient().get(1);
            var numeratorDenominatorX = new PureNumeratorDenominatorDto(xNum, xDen);
            var numeratorDenominatorY = new PureNumeratorDenominatorDto(yNum, yDen);

            currentRate.setAvgCorrelationX(xAvgCorrelationMap.get(numeratorDenominatorX));
            currentRate.setAvgCorrelationY(yAvgCorrelationMap.get(numeratorDenominatorY));
        }
    }
}
