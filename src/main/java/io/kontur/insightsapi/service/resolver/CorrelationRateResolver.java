package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.ForAvgCorrelationDto;
import io.kontur.insightsapi.dto.NodeDto;
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
        Map<NodeDto, Set<NodeDto>> graph = createGraph(correlationRateList);

        Map<List<String>, List<String>> xChildParent = new HashMap<>();
        Map<List<String>, List<String>> yChildParent = new HashMap<>();

        bfs(graph, importantLayers, xChildParent, yChildParent);

        setParent(correlationRateList, xChildParent, yChildParent);
    }

    private Map<NodeDto, Set<NodeDto>> createGraph(List<PolygonCorrelationRate> correlationRateList) {
        Map<NodeDto, Set<NodeDto>> graph = new HashMap<>();
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > highCorrelationLevel) {
                NodeDto nodeDtoX = new NodeDto(polygonCorrelationRate.getX().getQuotient(),
                        polygonCorrelationRate.getAvgCorrelationX(), polygonCorrelationRate.getAvgCorrelationY());
                NodeDto nodeDtoY = new NodeDto(polygonCorrelationRate.getY().getQuotient(),
                        polygonCorrelationRate.getAvgCorrelationX(), polygonCorrelationRate.getAvgCorrelationY());
                if (!graph.containsKey(nodeDtoX)) {
                    graph.put(nodeDtoX, new HashSet<>());
                }
                graph.get(nodeDtoX).add(nodeDtoY);
                if (!graph.containsKey(nodeDtoY)) {
                    graph.put(nodeDtoY, new HashSet<>());
                }
                graph.get(nodeDtoY).add(nodeDtoX);
            }
        }
        return graph;
    }

    private void bfs(Map<NodeDto, Set<NodeDto>> graph, Set<List<String>> importantLayers,
                     Map<List<String>, List<String>> xChildParent,
                     Map<List<String>, List<String>> yChildParent) {
        Set<NodeDto> viewed = new HashSet<>();
        for (Map.Entry<NodeDto, Set<NodeDto>> entry : graph.entrySet()) {
            if (!viewed.contains(entry.getKey())) {
                Set<NodeDto> connectedComponent = new HashSet<>();
                connectedComponent.add(entry.getKey());
                viewed.add(entry.getKey());
                Queue<NodeDto> queue = new LinkedList<>(entry.getValue());
                while (!queue.isEmpty()) {
                    NodeDto currentNode = queue.poll();
                    if (!viewed.contains(currentNode)) {
                        viewed.add(currentNode);
                        connectedComponent.add(currentNode);
                        if (graph.containsKey(currentNode)) {
                            queue.addAll(graph.get(currentNode));
                        }
                    }
                }
                //find parent for all elements in connected component
                fillParentConnectedComponent(connectedComponent, importantLayers, xChildParent, yChildParent);
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

    private void fillParentConnectedComponent(Set<NodeDto> viewed, Set<List<String>> importantLayers,
                                              Map<List<String>, List<String>> xChildParent,
                                              Map<List<String>, List<String>> yChildParent) {
        List<NodeDto> foundImportantLayers = findImportantLayers(viewed, importantLayers);
        switch (foundImportantLayers.size()) {
            case 0:
                fillParentWhenNoImportantLayers(viewed, xChildParent, yChildParent);
                break;
            case 1:
                fillParentWhenOneImportantLayer(viewed, xChildParent, yChildParent, foundImportantLayers);
                break;
            default:
                fillParentWhenSeveralImportantLayers(viewed, importantLayers, xChildParent, yChildParent, foundImportantLayers);
        }
    }

    private void fillParentWhenNoImportantLayers(Set<NodeDto> viewed, Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent) {
        List<String> parentX = viewed.stream()
                .min(Comparator.comparing(NodeDto::getAvgCorrelationX))
                .map(NodeDto::getQuotient)
                .orElse(null);
        List<String> parentY = viewed.stream()
                .min(Comparator.comparing(NodeDto::getAvgCorrelationY))
                .map(NodeDto::getQuotient)
                .orElse(null);
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.getQuotient().equals(parentX)) {
                xChildParent.put(currentViewed.getQuotient(), parentX);
            }
            if (!currentViewed.getQuotient().equals(parentY)) {
                yChildParent.put(currentViewed.getQuotient(), parentY);
            }
        }
    }

    private void fillParentWhenOneImportantLayer(Set<NodeDto> viewed, Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent, List<NodeDto> foundImportantLayers) {
        List<String> parent = foundImportantLayers.get(0).getQuotient();
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.getQuotient().equals(parent)) {
                xChildParent.put(currentViewed.getQuotient(), parent);
                yChildParent.put(currentViewed.getQuotient(), parent);
            }
        }
    }

    private void fillParentWhenSeveralImportantLayers(Set<NodeDto> viewed, Set<List<String>> importantLayers,
                                                      Map<List<String>, List<String>> xChildParent,
                                                      Map<List<String>, List<String>> yChildParent,
                                                      List<NodeDto> foundImportantLayers) {
        List<String> parentX = foundImportantLayers.stream()
                .min(Comparator.comparing(NodeDto::getAvgCorrelationX))
                .map(NodeDto::getQuotient)
                .orElse(null);
        List<String> parentY = foundImportantLayers.stream()
                .min(Comparator.comparing(NodeDto::getAvgCorrelationY))
                .map(NodeDto::getQuotient)
                .orElse(null);
        for (NodeDto currentViewed : viewed) {
            if (!importantLayers.contains(currentViewed.getQuotient())) {
                xChildParent.put(currentViewed.getQuotient(), parentX);
                yChildParent.put(currentViewed.getQuotient(), parentY);
            }
        }
    }

    private List<NodeDto> findImportantLayers(Set<NodeDto> viewed, Set<List<String>> importantLayers) {
        List<NodeDto> result = new ArrayList<>();
        for (NodeDto currentViewed : viewed) {
            if (importantLayers.contains(currentViewed.getQuotient())) {
                result.add(currentViewed);
            }
        }
        return result;
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
