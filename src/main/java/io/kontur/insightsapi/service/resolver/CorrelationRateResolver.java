package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.ForAvgCorrelationDto;
import io.kontur.insightsapi.dto.GraphDto;
import io.kontur.insightsapi.dto.NumeratorsDenominatorsDto;
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
            fillParent(correlationRateList, importantLayers);
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

        fillParent(correlationRateList, importantLayers);
        return correlationRateList;
    }

    private void fillParent(List<PolygonCorrelationRate> correlationRateList, Set<List<String>> importantLayers) {
        GraphDto graph = createGraph(correlationRateList);

        Map<List<String>, List<String>> xChildParent = new HashMap<>();
        Map<List<String>, List<String>> yChildParent = new HashMap<>();

        bfs(graph, importantLayers, xChildParent, yChildParent);

        setParent(correlationRateList, xChildParent, yChildParent);
    }

    private GraphDto createGraph(List<PolygonCorrelationRate> correlationRateList) {
        Map<List<String>, Double> xAvgCorrelation = new HashMap<>();
        Map<List<String>, Double> yAvgCorrelation = new HashMap<>();
        Map<List<String>, Set<List<String>>> graph = new HashMap<>();
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > highCorrelationLevel) {
                List<String> x = polygonCorrelationRate.getX().getQuotient();
                if (!xAvgCorrelation.containsKey(x)) {
                    xAvgCorrelation.put(x, polygonCorrelationRate.getAvgCorrelationX());
                }
                List<String> y = polygonCorrelationRate.getY().getQuotient();
                if (!yAvgCorrelation.containsKey(y)) {
                    yAvgCorrelation.put(y, polygonCorrelationRate.getAvgCorrelationY());
                }
                if (!graph.containsKey(x)) {
                    graph.put(x, new HashSet<>());
                }
                graph.get(x).add(y);
                if (!graph.containsKey(y)) {
                    graph.put(y, new HashSet<>());
                }
                graph.get(y).add(x);
            }
        }
        return GraphDto.builder()
                .graph(graph)
                .xAvgCorrelation(xAvgCorrelation)
                .yAvgCorrelation(yAvgCorrelation)
                .build();
    }

    private void bfs(GraphDto graphDto, Set<List<String>> importantLayers,
                     Map<List<String>, List<String>> xChildParent,
                     Map<List<String>, List<String>> yChildParent) {
        Map<List<String>, Set<List<String>>> graph = graphDto.getGraph();
        Set<List<String>> viewed = new HashSet<>();
        for (Map.Entry<List<String>, Set<List<String>>> entry : graph.entrySet()) {
            if (!viewed.contains(entry.getKey())) {
                Set<List<String>> connectedComponent = new HashSet<>();
                connectedComponent.add(entry.getKey());
                viewed.add(entry.getKey());
                Queue<List<String>> queue = new LinkedList<>(entry.getValue());
                while (!queue.isEmpty()) {
                    List<String> currentNode = queue.poll();
                    if (!viewed.contains(currentNode)) {
                        viewed.add(currentNode);
                        connectedComponent.add(currentNode);
                        if (graph.containsKey(currentNode)) {
                            queue.addAll(graph.get(currentNode));
                        }
                    }
                }
                //find parent for all elements in connected component
                fillParentConnectedComponent(connectedComponent, importantLayers, graphDto.getXAvgCorrelation(),
                        graphDto.getYAvgCorrelation(), xChildParent, yChildParent);
            }
        }
    }

    private void setParent(List<PolygonCorrelationRate> correlationRateList, Map<List<String>, List<String>> xChildParent,
                           Map<List<String>, List<String>> yChildParent) {
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

    private void fillParentConnectedComponent(Set<List<String>> viewed, Set<List<String>> importantLayers,
                                              Map<List<String>, Double> xAvgCorrelation,
                                              Map<List<String>, Double> yAvgCorrelation,
                                              Map<List<String>, List<String>> xChildParent,
                                              Map<List<String>, List<String>> yChildParent) {
        Set<List<String>> foundImportantLayers = findImportantLayers(viewed, importantLayers);
        switch (foundImportantLayers.size()) {
            case 0:
                fillParentWhenNoImportantLayers(viewed, xAvgCorrelation, yAvgCorrelation, xChildParent, yChildParent);
                break;
            case 1:
                fillParentWhenOneImportantLayer(viewed, xChildParent, yChildParent, foundImportantLayers);
                break;
            default:
                fillParentWhenSeveralImportantLayers(viewed, xAvgCorrelation, yAvgCorrelation, importantLayers,
                        xChildParent, yChildParent, foundImportantLayers);
        }
    }

    private void fillParentWhenNoImportantLayers(Set<List<String>> viewed, Map<List<String>, Double> xAvgCorrelation,
                                                 Map<List<String>, Double> yAvgCorrelation,
                                                 Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent) {
        List<String> parentX = findLayerWithMinAvgCorrelation(viewed, xAvgCorrelation);
        List<String> parentY = findLayerWithMinAvgCorrelation(viewed, yAvgCorrelation);
        for (List<String> currentViewed : viewed) {
            if (!currentViewed.equals(parentX)) {
                xChildParent.put(currentViewed, parentX);
            }
            if (!currentViewed.equals(parentY)) {
                yChildParent.put(currentViewed, parentY);
            }
        }
    }

    private void fillParentWhenOneImportantLayer(Set<List<String>> viewed, Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent,
                                                 Set<List<String>> foundImportantLayers) {
        List<String> parent = foundImportantLayers.stream()
                .findFirst()
                .orElse(null);
        for (List<String> currentViewed : viewed) {
            if (!currentViewed.equals(parent)) {
                xChildParent.put(currentViewed, parent);
                yChildParent.put(currentViewed, parent);
            }
        }
    }

    private void fillParentWhenSeveralImportantLayers(Set<List<String>> viewed, Map<List<String>, Double> xAvgCorrelation,
                                                      Map<List<String>, Double> yAvgCorrelation,
                                                      Set<List<String>> importantLayers,
                                                      Map<List<String>, List<String>> xChildParent,
                                                      Map<List<String>, List<String>> yChildParent,
                                                      Set<List<String>> foundImportantLayers) {
        List<String> parentX = findLayerWithMinAvgCorrelation(foundImportantLayers, xAvgCorrelation);
        List<String> parentY = findLayerWithMinAvgCorrelation(foundImportantLayers, yAvgCorrelation);
        for (List<String> currentViewed : viewed) {
            if (!importantLayers.contains(currentViewed)) {
                xChildParent.put(currentViewed, parentX);
                yChildParent.put(currentViewed, parentY);
            }
        }
    }

    private Set<List<String>> findImportantLayers(Set<List<String>> viewed, Set<List<String>> importantLayers) {
        Set<List<String>> result = new HashSet<>();
        for (List<String> currentViewed : viewed) {
            if (importantLayers.contains(currentViewed)) {
                result.add(currentViewed);
            }
        }
        return result;
    }

    private List<String> findLayerWithMinAvgCorrelation(Set<List<String>> viewed, Map<List<String>, Double> avgCorrelation) {
        return viewed.stream()
                .filter(avgCorrelation::containsKey)
                .min(Comparator.comparing(avgCorrelation::get))
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
