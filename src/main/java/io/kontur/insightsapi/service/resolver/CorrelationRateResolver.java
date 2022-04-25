package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.*;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonCorrelationRate;
import io.kontur.insightsapi.service.GeometryTransformer;
import io.kontur.insightsapi.service.cacheable.CorrelationRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CorrelationRateResolver implements GraphQLResolver<BivariateStatistic> {

    private final CorrelationRateService correlationRateService;

    private final GeometryTransformer geometryTransformer;

    @Value("${bivatiateMatrix.highCorrelationLevel}")
    private Double highCorrelationLevel;

    public List<PolygonCorrelationRate> getCorrelationRates(BivariateStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getParent().getArguments().get("polygonStatisticRequest");
        var importantLayers = getImportantLayers(arguments);
        //no polygons defined
        if (!arguments.containsKey("polygon") && !arguments.containsKey("polygonV2")) {
            var correlationRateList = correlationRateService.getAllCorrelationRateStatistics();
            fillParent(correlationRateList, importantLayers);
            return correlationRateList;
        }
        var transformedGeometry = getPolygon(arguments);

        //get numr & denm from bivariate_axis & bivariate_indicators size nearly 17k
        List<NumeratorsDenominatorsDto> numeratorsDenominatorsDtos = correlationRateService.getNumeratorsDenominatorsForCorrelation();

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

    private void fillParent(List<PolygonCorrelationRate> correlationRateList, List<List<String>> importantLayers) {
        GraphDto graph = createGraph(correlationRateList);

        Map<List<String>, List<String>> xChildParent = new HashMap<>();
        Map<List<String>, List<String>> yChildParent = new HashMap<>();

        bfs(graph, importantLayers, xChildParent, yChildParent);

        setParent(correlationRateList, xChildParent, yChildParent);
    }

    private GraphDto createGraph(List<PolygonCorrelationRate> correlationRateList) {
        Map<NodeDto, Double> xAvgCorrelation = new HashMap<>();
        Map<NodeDto, Double> yAvgCorrelation = new HashMap<>();
        Map<NodeDto, Set<NodeDto>> graph = new HashMap<>();
        for (PolygonCorrelationRate polygonCorrelationRate : correlationRateList) {
            if (Math.abs(polygonCorrelationRate.getCorrelation()) > highCorrelationLevel) {
                NodeDto xNode = new NodeDto(polygonCorrelationRate.getX().getQuotient(), "x");
                if (!xAvgCorrelation.containsKey(xNode)) {
                    xAvgCorrelation.put(xNode, polygonCorrelationRate.getAvgCorrelationX());
                }
                NodeDto yNode = new NodeDto(polygonCorrelationRate.getY().getQuotient(), "y");
                if (!yAvgCorrelation.containsKey(yNode)) {
                    yAvgCorrelation.put(yNode, polygonCorrelationRate.getAvgCorrelationY());
                }
                if (!graph.containsKey(xNode)) {
                    graph.put(xNode, new HashSet<>());
                }
                graph.get(xNode).add(yNode);
                if (!graph.containsKey(yNode)) {
                    graph.put(yNode, new HashSet<>());
                }
                graph.get(yNode).add(xNode);
            }
        }
        return GraphDto.builder()
                .graph(graph)
                .xAvgCorrelation(xAvgCorrelation)
                .yAvgCorrelation(yAvgCorrelation)
                .build();
    }

    private void bfs(GraphDto graphDto, List<List<String>> importantLayers,
                     Map<List<String>, List<String>> xChildParent,
                     Map<List<String>, List<String>> yChildParent) {
        Map<NodeDto, Set<NodeDto>> graph = graphDto.getGraph();
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

    private void fillParentConnectedComponent(Set<NodeDto> viewed, List<List<String>> importantLayers,
                                              Map<NodeDto, Double> xAvgCorrelation,
                                              Map<NodeDto, Double> yAvgCorrelation,
                                              Map<List<String>, List<String>> xChildParent,
                                              Map<List<String>, List<String>> yChildParent) {
        Set<NodeDto> foundImportantLayers = findImportantLayers(viewed, importantLayers);
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

    private void fillParentWhenNoImportantLayers(Set<NodeDto> viewed, Map<NodeDto, Double> xAvgCorrelation,
                                                 Map<NodeDto, Double> yAvgCorrelation,
                                                 Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent) {
        NodeDto parentX = findLayerWithMinAvgCorrelation(viewed, xAvgCorrelation, "x");
        NodeDto parentY = findLayerWithMinAvgCorrelation(viewed, yAvgCorrelation, "y");
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parentX) && currentViewed.getAxis().equals("x")) {
                xChildParent.put(currentViewed.getQuotient(), parentX.getQuotient());
            }
            if (!currentViewed.equals(parentY) && currentViewed.getAxis().equals("y")) {
                yChildParent.put(currentViewed.getQuotient(), parentY.getQuotient());
            }
        }
    }

    private void fillParentWhenOneImportantLayer(Set<NodeDto> viewed, Map<List<String>, List<String>> xChildParent,
                                                 Map<List<String>, List<String>> yChildParent,
                                                 Set<NodeDto> foundImportantLayers) {
        NodeDto parentX = foundImportantLayers.stream()
                .filter(l -> l.getAxis().equals("x"))
                .findFirst()
                .orElse(new NodeDto(null, "x"));
        NodeDto parentY = foundImportantLayers.stream()
                .filter(l -> l.getAxis().equals("y"))
                .findFirst()
                .orElse(new NodeDto(null, "y"));
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parentX) && currentViewed.getAxis().equals("x")) {
                xChildParent.put(currentViewed.getQuotient(), parentX.getQuotient());
            }
            if (!currentViewed.equals(parentY) && currentViewed.getAxis().equals("y")) {
                yChildParent.put(currentViewed.getQuotient(), parentY.getQuotient());
            }
        }
    }

    private void fillParentWhenSeveralImportantLayers(Set<NodeDto> viewed, Map<NodeDto, Double> xAvgCorrelation,
                                                      Map<NodeDto, Double> yAvgCorrelation,
                                                      List<List<String>> importantLayers,
                                                      Map<List<String>, List<String>> xChildParent,
                                                      Map<List<String>, List<String>> yChildParent,
                                                      Set<NodeDto> foundImportantLayers) {
        NodeDto parentX = findLayerWithMinAvgCorrelation(foundImportantLayers, xAvgCorrelation, "x");
        NodeDto parentY = findLayerWithMinAvgCorrelation(foundImportantLayers, yAvgCorrelation, "y");
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parentX) && currentViewed.getAxis().equals("x")
                    && !importantLayers.contains(currentViewed.getQuotient())) {
                xChildParent.put(currentViewed.getQuotient(), parentX.getQuotient());
            }
            if (!currentViewed.equals(parentY) && currentViewed.getAxis().equals("y")
                    && !importantLayers.contains(currentViewed.getQuotient())) {
                yChildParent.put(currentViewed.getQuotient(), parentY.getQuotient());
            }
        }
    }

    private Set<NodeDto> findImportantLayers(Set<NodeDto> viewed, List<List<String>> importantLayers) {
        Set<NodeDto> result = new HashSet<>();
        Set<List<String>> viewedQuotient = viewed.stream()
                .map(NodeDto::getQuotient)
                .collect(Collectors.toSet());
        for (List<String> currentViewed : viewedQuotient) {
            if (importantLayers.contains(currentViewed)) {
                result.add(new NodeDto(currentViewed, "x"));
                result.add(new NodeDto(currentViewed, "y"));
            }
        }
        return result;
    }

    private NodeDto findLayerWithMinAvgCorrelation(Set<NodeDto> viewed, Map<NodeDto, Double> avgCorrelation,
                                                   String axis) {
        return viewed.stream()
                .filter(v -> avgCorrelation.containsKey(v) && v.getAxis().equals(axis))
                .min(Comparator.comparing(avgCorrelation::get))
                .orElse(new NodeDto(null, axis));
    }

    private Comparator<PolygonCorrelationRate> correlationRateComparator() {
        return Comparator.<PolygonCorrelationRate, Double>comparing(c -> c.getAvgCorrelationX() * c.getAvgCorrelationY()).reversed();
    }

    private List<PolygonCorrelationRate> calculatePolygonCorrelations(List<NumeratorsDenominatorsDto> sourceDtoList,
                                                                      String transformedGeometry) {
        List<PolygonCorrelationRate> result = new ArrayList<>();

        //run for every 500 bivariative_axis sourceDtoList size = 500 & get correlationList
        List<Double> correlations = correlationRateService.getPolygonCorrelationRateStatisticsBatch(sourceDtoList, transformedGeometry);
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

    private List<List<String>> getImportantLayers(Map<String, Object> arguments) {
        if (arguments.containsKey("importantLayers")) {
            return (List<List<String>>) arguments.get("importantLayers");
        }
        return new ArrayList<>();
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
                correlationRateService.getNumeratorsForNotEmptyLayersBatch(numeratorsDenominatorsDtos, transformedGeometry);
        return numeratorsDenominatorsDtos.stream()
                .filter(dto -> (numeratorsForNotEmptyLayers.containsKey(dto.getXNumerator())
                        && numeratorsForNotEmptyLayers.get(dto.getXNumerator()))
                        && (numeratorsForNotEmptyLayers.containsKey(dto.getYNumerator())
                        && numeratorsForNotEmptyLayers.get(dto.getYNumerator())))
                .collect(Collectors.toList());
    }
}
