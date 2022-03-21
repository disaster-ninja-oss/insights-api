package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.ForAvgCorrelationDto;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
import io.kontur.insightsapi.dto.ParentDto;
import io.kontur.insightsapi.dto.PureNumeratorDenominatorDto;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CorrelationRateResolver implements GraphQLResolver<BivariateStatistic> {

    private final StatisticRepository statisticRepository;

    private final GeometryTransformer geometryTransformer;

    @Value("${bivatiateMatrix.highCorrelationLevel}")
    private Double highCorrelationLevel;

    public List<PolygonCorrelationRate> getCorrelationRates(BivariateStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getParent().getArguments().get("polygonStatisticRequest");
        var importantLayers = getImportantLayers(arguments);
        //no polygons defined
        if (!arguments.containsKey("polygon") && !arguments.containsKey("polygonV2")) {
            var correlationRateList = statisticRepository.getAllCorrelationRateStatistics();
            fillParentV1(correlationRateList, importantLayers);
            return correlationRateList;
        }
        var transformedGeometry = getPolygon(arguments);

        //get numr & denm from bivariate_axis & bivariate_indicators size nearly 17k
        List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos = statisticRepository.getNumeratorsDenominatorsForCorrelation();

        String finalTransformedGeometry = transformedGeometry;

        //find numerators for not empty layers
        List<NumeratorsDenominatorsDto> numeratorsDenominatorsForNotEmptyLayers =
                Lists.partition(numeratorsDenominatorsDtos, 500).parallelStream()
                        .map(sourceDtoList -> findNumeratorsDenominatorsForNotEmptyLayers(sourceDtoList, finalTransformedGeometry))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        //calculate correlation for every bivariate_axis in defined polygon that intersects h3
        var correlationRateList = Lists.partition(numeratorsDenominatorsForNotEmptyLayers, 500).parallelStream()
                .map(sourceDtoList -> calculatePolygonCorrelations(sourceDtoList, finalTransformedGeometry))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //calculate avg correlation for xy
        var avgCorrelationXY = calculateAvgCorrelationXY(correlationRateList);

        //set avg correlation rate
        fillAvgCorrelation(correlationRateList, avgCorrelationXY);

        //should be sorted with correlationRateComparator
        correlationRateList.sort(correlationRateComparator());

        fillParentV1(correlationRateList, importantLayers);
        return correlationRateList;
    }

    private void fillParent(List<PolygonCorrelationRate> correlationRateList, Set<List<String>> importantLayers) {
        //Set<List<String>> xChildren = new HashSet<>();
        //Set<List<String>> yChildren = new HashSet<>();
//        Map<List<String>, Set<List<String>>> xParentChildren = new HashMap<>();
//        Map<List<String>, Set<List<String>>> yParentChildren = new HashMap<>();
//        Map<List<String>, Set<ParentDto>> xChildParents = new HashMap<>();
//        Map<List<String>, Set<ParentDto>> yChildParents = new HashMap<>();
        List<String> baseIndicators = statisticRepository.getBaseIndicators();
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > highCorrelationLevel) {
                if (polygonCorrelationRate.getAvgCorrelationX() > polygonCorrelationRate.getAvgCorrelationY()
                        && !importantLayers.contains(polygonCorrelationRate.getX().getQuotient())
                        //&& !xChildren.contains(polygonCorrelationRate.getX().getQuotient())
                        && !baseIndicators.contains(polygonCorrelationRate.getY().getQuotient().get(0))) {


//                    if (!xParentChildren.containsKey(polygonCorrelationRate.getX().getQuotient())) {
////                        polygonCorrelationRate.getX().setParent(polygonCorrelationRate.getY().getQuotient());
////                        xChildren.add(polygonCorrelationRate.getX().getQuotient());
//                        if (!xParentChildren.containsKey(polygonCorrelationRate.getY().getQuotient())) {
//                            xParentChildren.put(polygonCorrelationRate.getY().getQuotient(), new HashSet<>());
//                        }
//                        xParentChildren.get(polygonCorrelationRate.getY().getQuotient()).add(polygonCorrelationRate.getX().getQuotient());
//
//                        if (!xChildParents.containsKey(polygonCorrelationRate.getX().getQuotient())) {
//                            xChildParents.put(polygonCorrelationRate.getX().getQuotient(), new HashSet<>());
//                        }
//                        xChildParents.get(polygonCorrelationRate.getX().getQuotient())
//                                .add(new ParentDto(polygonCorrelationRate.getY().getQuotient(), polygonCorrelationRate.getAvgCorrelationY()));
//                    } else {
//                        if (!xParentChildren.get(polygonCorrelationRate.getX().getQuotient()).contains(polygonCorrelationRate.getY().getQuotient())) {
////                            polygonCorrelationRate.getX().setParent(polygonCorrelationRate.getY().getQuotient());
////                            xChildren.add(polygonCorrelationRate.getX().getQuotient());
//                            if (!xParentChildren.containsKey(polygonCorrelationRate.getY().getQuotient())) {
//                                xParentChildren.put(polygonCorrelationRate.getY().getQuotient(), new HashSet<>());
//                            }
//                            xParentChildren.get(polygonCorrelationRate.getY().getQuotient()).add(polygonCorrelationRate.getX().getQuotient());
//
//                            if (!xChildParents.containsKey(polygonCorrelationRate.getX().getQuotient())) {
//                                xChildParents.put(polygonCorrelationRate.getX().getQuotient(), new HashSet<>());
//                            }
//                            xChildParents.get(polygonCorrelationRate.getX().getQuotient())
//                                    .add(new ParentDto(polygonCorrelationRate.getY().getQuotient(), polygonCorrelationRate.getAvgCorrelationY()));
//                        }
//                    }
                }
                if (polygonCorrelationRate.getAvgCorrelationY() > polygonCorrelationRate.getAvgCorrelationX()
                        && !importantLayers.contains(polygonCorrelationRate.getY().getQuotient())) {
                    //&& !yChildren.contains(polygonCorrelationRate.getY().getQuotient())) {
//                    if (!yParentChildren.containsKey(polygonCorrelationRate.getY().getQuotient())) {
////                        polygonCorrelationRate.getY().setParent(polygonCorrelationRate.getX().getQuotient());
////                        yChildren.add(polygonCorrelationRate.getY().getQuotient());
//                        if (!yParentChildren.containsKey(polygonCorrelationRate.getX().getQuotient())) {
//                            yParentChildren.put(polygonCorrelationRate.getX().getQuotient(), new HashSet<>());
//                        }
//                        yParentChildren.get(polygonCorrelationRate.getX().getQuotient()).add(polygonCorrelationRate.getY().getQuotient());
//
//                        if (!yChildParents.containsKey(polygonCorrelationRate.getY().getQuotient())) {
//                            yChildParents.put(polygonCorrelationRate.getY().getQuotient(), new HashSet<>());
//                        }
//                        yChildParents.get(polygonCorrelationRate.getY().getQuotient())
//                                .add(new ParentDto(polygonCorrelationRate.getX().getQuotient(), polygonCorrelationRate.getAvgCorrelationX()));
//                    } else {
//                        if (!yParentChildren.get(polygonCorrelationRate.getY().getQuotient()).contains(polygonCorrelationRate.getX().getQuotient())) {
////                            polygonCorrelationRate.getY().setParent(polygonCorrelationRate.getX().getQuotient());
////                            yChildren.add(polygonCorrelationRate.getY().getQuotient());
//                            if (!yParentChildren.containsKey(polygonCorrelationRate.getX().getQuotient())) {
//                                yParentChildren.put(polygonCorrelationRate.getX().getQuotient(), new HashSet<>());
//                            }
//                            yParentChildren.get(polygonCorrelationRate.getX().getQuotient()).add(polygonCorrelationRate.getY().getQuotient());
//
//                            if (!yChildParents.containsKey(polygonCorrelationRate.getY().getQuotient())) {
//                                yChildParents.put(polygonCorrelationRate.getY().getQuotient(), new HashSet<>());
//                            }
//                            yChildParents.get(polygonCorrelationRate.getY().getQuotient())
//                                    .add(new ParentDto(polygonCorrelationRate.getX().getQuotient(), polygonCorrelationRate.getAvgCorrelationX()));
//                        }
//                    }
                }
            }
        }

//        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
//            List<String> quotientX = polygonCorrelationRate.getX().getQuotient();
//            List<String> quotientY = polygonCorrelationRate.getY().getQuotient();
//            if (xChildParents.containsKey(quotientX)) {
//                polygonCorrelationRate.getX()
//                        .setParent(xChildParents.get(quotientX).stream()
//                                .min(Comparator.comparing(ParentDto::getAvgCorrelation))
//                                .get()
//                                .getQuotient());
//            }
//            if (yChildParents.containsKey(quotientY)) {
//                polygonCorrelationRate.getY()
//                        .setParent(yChildParents.get(quotientY).stream()
//                                .min(Comparator.comparing(ParentDto::getAvgCorrelation))
//                                .get()
//                                .getQuotient());
//            }
//        }
    }

    private void fillParentV1(List<PolygonCorrelationRate> correlationRateList, Set<List<String>> importantLayers) {
        List<String> baseIndicators = statisticRepository.getBaseIndicators();
        Map<List<String>, Set<ParentDto>> xChildParents = new HashMap<>();
        Map<List<String>, Set<ParentDto>> yChildParents = new HashMap<>();
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > highCorrelationLevel) {
                if (polygonCorrelationRate.getAvgCorrelationX() > polygonCorrelationRate.getAvgCorrelationY()
                        && !importantLayers.contains(polygonCorrelationRate.getX().getQuotient())
                        && !baseIndicators.contains(polygonCorrelationRate.getY().getQuotient().get(0))) {
                    if (!xChildParents.containsKey(polygonCorrelationRate.getX().getQuotient())) {
                        xChildParents.put(polygonCorrelationRate.getX().getQuotient(), new HashSet<>());
                    }
                    xChildParents.get(polygonCorrelationRate.getX().getQuotient())
                            .add(new ParentDto(polygonCorrelationRate.getY().getQuotient(), polygonCorrelationRate.getAvgCorrelationY()));
                }
                if (polygonCorrelationRate.getAvgCorrelationY() > polygonCorrelationRate.getAvgCorrelationX()
                        && !importantLayers.contains(polygonCorrelationRate.getY().getQuotient())) {
                    if (!yChildParents.containsKey(polygonCorrelationRate.getY().getQuotient())) {
                        yChildParents.put(polygonCorrelationRate.getY().getQuotient(), new HashSet<>());
                    }
                    yChildParents.get(polygonCorrelationRate.getY().getQuotient())
                            .add(new ParentDto(polygonCorrelationRate.getX().getQuotient(), polygonCorrelationRate.getAvgCorrelationX()));
                }
            }
        }
        Map<List<String>, List<String>> xChildParent = calculateChildParentMap(xChildParents);
        Map<List<String>, List<String>> yChildParent = calculateChildParentMap(yChildParents);
//        Map<List<String>, List<String>> xChildParent = new HashMap<>();
//        for (Map.Entry<List<String>, Set<ParentDto>> entry : xChildParents.entrySet()) {
//            List<String> currentChild = entry.getKey();
//            ParentDto bestParent = findBestParent(entry.getValue());
//            Queue<ParentDto> queue = new LinkedList<>();
//            queue.add(bestParent);
//            Set<List<String>> viewed = new HashSet<>();
//            viewed.add(currentChild);
//            ParentDto currentParent = null;
//            while (!queue.isEmpty()) {
//                currentParent = queue.poll();
//                if (!viewed.contains(currentParent.getQuotient())) {
//                    viewed.add(currentParent.getQuotient());
//                    if (xChildParents.containsKey(currentParent.getQuotient())) {
//                        queue.add(findBestParent(xChildParents.get(currentParent.getQuotient())));
//                    }
//                }
//            }
//            xChildParent.put(currentChild, currentParent.getQuotient());
//        }

        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            List<String> quotientX = polygonCorrelationRate.getX().getQuotient();
            List<String> quotientY = polygonCorrelationRate.getY().getQuotient();
            if (xChildParent.containsKey(quotientX)) {
                polygonCorrelationRate.getX()
                        .setParent(xChildParent.get(quotientX));
            }
            if (yChildParent.containsKey(quotientY)) {
                polygonCorrelationRate.getY()
                        .setParent(yChildParent.get(quotientY));
            }
        }
    }

    private Map<List<String>, List<String>> calculateChildParentMap(Map<List<String>, Set<ParentDto>> childParents){
        Map<List<String>, List<String>> childParent = new HashMap<>();
        for (Map.Entry<List<String>, Set<ParentDto>> entry : childParents.entrySet()) {
            List<String> currentChild = entry.getKey();
            ParentDto bestParent = findBestParent(entry.getValue());
            Queue<ParentDto> queue = new LinkedList<>();
            queue.add(bestParent);
            Set<List<String>> viewed = new HashSet<>();
            viewed.add(currentChild);
            ParentDto currentParent = null;
            while (!queue.isEmpty()) {
                currentParent = queue.poll();
                if (!viewed.contains(currentParent.getQuotient())) {
                    viewed.add(currentParent.getQuotient());
                    if (childParents.containsKey(currentParent.getQuotient())) {
                        queue.add(findBestParent(childParents.get(currentParent.getQuotient())));
                    }
                }
            }
            childParent.put(currentChild, currentParent.getQuotient());
        }
        return childParent;
    }

    private ParentDto findBestParent(Set<ParentDto> parents) {
        return parents.stream()
                .min(Comparator.comparing(ParentDto::getAvgCorrelation))
                .get();
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

    private Set<List<String>> getImportantLayers(Map<String, Object> arguments) {
        if (arguments.containsKey("importantLayers")) {
            return Sets.newHashSet((List<List<String>>) arguments.get("importantLayers"));
        }
        return new HashSet<>();
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

    private List<NumeratorsDenominatorsDto> findNumeratorsDenominatorsForNotEmptyLayers(List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos,
                                                                                        String transformedGeometry) {
        Map<String, Boolean> numeratorsForNotEmptyLayers =
                statisticRepository.getNumeratorsForNotEmptyLayersBatch(numeratorsDenominatorsDtos, transformedGeometry);
        return numeratorsDenominatorsDtos.stream()
                .filter(dto -> (numeratorsForNotEmptyLayers.containsKey(dto.getXNumerator())
                        && numeratorsForNotEmptyLayers.get(dto.getXNumerator()))
                        && (numeratorsForNotEmptyLayers.containsKey(dto.getYNumerator())
                        && numeratorsForNotEmptyLayers.get(dto.getYNumerator())))
                .collect(Collectors.toList());
    }
}
