package io.kontur.insightsapi.service.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import io.kontur.insightsapi.dto.*;
import io.kontur.insightsapi.model.Axis;
import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonMetrics;
import io.kontur.insightsapi.repository.StatisticRepository;
import io.kontur.insightsapi.service.Helper;
import io.kontur.insightsapi.service.MetricsHelper;
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

    private final MetricsHelper metricsHelper;

    private final StatisticRepository statisticRepository;

    private final Helper helper;

    @Value("${bivatiateMatrix.highCorrelationLevel}")
    private Double highCorrelationLevel;

    public List<PolygonMetrics> getCorrelationRates(BivariateStatistic statistic, DataFetchingEnvironment environment) throws JsonProcessingException {
        Map<String, Object> arguments = (Map<String, Object>) environment.getExecutionStepInfo()
                .getParent().getParent().getArguments().get("polygonStatisticRequest");
        var importantLayers = getImportantLayers(arguments);
        //no polygons defined
        if (!arguments.containsKey("polygon") && !arguments.containsKey("polygonV2")) {
            var correlationRateList = correlationRateService.getAllCorrelationRateStatistics().stream()
                    .map(PolygonMetrics::clone)
                    .collect(Collectors.toList());
            fillParent(correlationRateList, importantLayers);
            return correlationRateList;
        }

        List<PolygonMetrics> correlationRateList;

        var transformedGeometry = metricsHelper.getPolygon(arguments);
        transformedGeometry = helper.transformGeometryToWkt(transformedGeometry);
        Map<NumeratorsDenominatorsUuidDto, NumeratorsDenominatorsDto> numeratorsDenominatorsMap = metricsHelper
                .getAllNumeratorsDenominators();
        correlationRateList = calculateAllPolygonCorrelations(numeratorsDenominatorsMap, transformedGeometry);

        //calculate avg correlation for xy
        var avgCorrelationXY = metricsHelper.calculateAvgMetricsXY(correlationRateList);

        //set avg correlation rate
        fillAvgCorrelation(correlationRateList, avgCorrelationXY);

        //should be sorted with correlationRateComparator
        correlationRateList.sort(correlationRateComparator());

        fillParent(correlationRateList, importantLayers);
        return correlationRateList;
    }

    private void fillParent(List<PolygonMetrics> correlationRateList, List<List<String>> importantLayers) {
        GraphDto graph = createGraph(correlationRateList);

        Map<List<String>, List<String>> xChildParent = new HashMap<>();
        Map<List<String>, List<String>> yChildParent = new HashMap<>();

        bfs(graph, importantLayers, xChildParent, yChildParent);

        setParent(correlationRateList, xChildParent, yChildParent);
    }

    private GraphDto createGraph(List<PolygonMetrics> correlationRateList) {
        Map<NodeDto, Double> xAvgCorrelation = new HashMap<>();
        Map<NodeDto, Double> yAvgCorrelation = new HashMap<>();
        Map<NodeDto, Set<NodeDto>> graph = new HashMap<>();
        for (PolygonMetrics polygonCorrelationRate : correlationRateList) {
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
                //x
                fillParentConnectedComponent(connectedComponent, importantLayers, graphDto.getXAvgCorrelation(),
                        xChildParent, "x");
                //y
                fillParentConnectedComponent(connectedComponent, importantLayers, graphDto.getYAvgCorrelation(),
                        yChildParent, "y");
            }
        }
    }

    private void setParent(List<PolygonMetrics> correlationRateList, Map<List<String>, List<String>> xChildParent,
                           Map<List<String>, List<String>> yChildParent) {
        for (PolygonMetrics polygonCorrelationRate : correlationRateList) {
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
                                              Map<NodeDto, Double> avgCorrelation,
                                              Map<List<String>, List<String>> childParent,
                                              String axis) {
        Set<NodeDto> viewedAxis = viewed.stream()
                .filter(v -> v.getAxis().equals(axis))
                .collect(Collectors.toSet());
        Set<NodeDto> foundImportantLayers = findImportantLayers(viewedAxis, importantLayers, axis);
        switch (foundImportantLayers.size()) {
            case 0:
                fillParentWhenNoImportantLayers(viewedAxis, avgCorrelation, childParent, axis);
                break;
            case 1:
                fillParentWhenOneImportantLayer(viewedAxis, childParent, foundImportantLayers, axis);
                break;
            default:
                fillParentWhenSeveralImportantLayers(viewedAxis, avgCorrelation, importantLayers,
                        childParent, foundImportantLayers, axis);
        }
    }

    private void fillParentWhenNoImportantLayers(Set<NodeDto> viewed, Map<NodeDto, Double> avgCorrelation,
                                                 Map<List<String>, List<String>> childParent,
                                                 String axis) {
        NodeDto parent = findLayerWithMinAvgCorrelation(viewed, avgCorrelation, axis);
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parent) && currentViewed.getAxis().equals(axis)) {
                childParent.put(currentViewed.getQuotient(), parent.getQuotient());
            }
        }
    }

    private void fillParentWhenOneImportantLayer(Set<NodeDto> viewed, Map<List<String>, List<String>> childParent,
                                                 Set<NodeDto> foundImportantLayers,
                                                 String axis) {
        NodeDto parent = foundImportantLayers.stream()
                .filter(l -> l.getAxis().equals(axis))
                .findFirst()
                .orElse(new NodeDto(null, axis));
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parent) && currentViewed.getAxis().equals(axis)) {
                childParent.put(currentViewed.getQuotient(), parent.getQuotient());
            }
        }
    }

    private void fillParentWhenSeveralImportantLayers(Set<NodeDto> viewed, Map<NodeDto, Double> avgCorrelation,
                                                      List<List<String>> importantLayers,
                                                      Map<List<String>, List<String>> childParent,
                                                      Set<NodeDto> foundImportantLayers,
                                                      String axis) {
        NodeDto parent = findLayerWithMinAvgCorrelation(foundImportantLayers, avgCorrelation, axis);
        for (NodeDto currentViewed : viewed) {
            if (!currentViewed.equals(parent) && currentViewed.getAxis().equals(axis)
                    && !importantLayers.contains(currentViewed.getQuotient())) {
                childParent.put(currentViewed.getQuotient(), parent.getQuotient());
            }
        }
    }

    private Set<NodeDto> findImportantLayers(Set<NodeDto> viewed, List<List<String>> importantLayers, String axis) {
        Set<NodeDto> result = new HashSet<>();
        Set<List<String>> viewedQuotient = viewed.stream()
                .map(NodeDto::getQuotient)
                .collect(Collectors.toSet());
        for (List<String> currentViewed : viewedQuotient) {
            if (importantLayers.contains(currentViewed)) {
                result.add(new NodeDto(currentViewed, axis));
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

    private Comparator<PolygonMetrics> correlationRateComparator() {
        return Comparator.<PolygonMetrics, Double>comparing(c -> c.getAvgCorrelationX() * c.getAvgCorrelationY()).reversed();
    }

    private List<PolygonMetrics> calculateAllPolygonCorrelations(Map<NumeratorsDenominatorsUuidDto, NumeratorsDenominatorsDto> sourceDtoMap,
                                                                 String transformedGeometry) {
        List<PolygonMetrics> result = new ArrayList<>();
        List<NumeratorsDenominatorsUuidCorrelationDto> correlations = correlationRateService
                .getPolygonCorrelationRateStatistics(transformedGeometry);
        for (NumeratorsDenominatorsUuidCorrelationDto correlation : correlations) {
            PolygonMetrics polygonCorrelationRate = new PolygonMetrics();
            var key = NumeratorsDenominatorsUuidDto.builder()
                    .xNumUuid(correlation.getXNumUuid())
                    .xDenUuid(correlation.getXDenUuid())
                    .yNumUuid(correlation.getYNumUuid())
                    .yDenUuid(correlation.getYDenUuid())
                    .build();
            NumeratorsDenominatorsDto sourceDto = sourceDtoMap.get(key);

            ///combine them on Axis with quality, correlation & rate
            if (sourceDto != null) {
                polygonCorrelationRate.setX(Axis.builder()
                        .label(sourceDto.getXLabel())
                        .quotient(List.of(sourceDto.getXNumerator(), sourceDto.getXDenominator()))
                        .build());
                polygonCorrelationRate.setY(Axis.builder()
                        .label(sourceDto.getYLabel())
                        .quotient(List.of(sourceDto.getYNumerator(), sourceDto.getYDenominator()))
                        .build());
                polygonCorrelationRate.setQuality(sourceDto.getQuality());
                polygonCorrelationRate.setCorrelation(correlation.getMetrics());
                polygonCorrelationRate.setMetrics(correlation.getMetrics());
                polygonCorrelationRate.setRate(correlation.getMetrics());
                result.add(polygonCorrelationRate);
            }
        }
        return result;
    }

    private List<List<String>> getImportantLayers(Map<String, Object> arguments) {
        if (arguments.containsKey("importantLayers")) {
            return (List<List<String>>) arguments.get("importantLayers");
        }
        return new ArrayList<>();
    }

    private void fillAvgCorrelation(List<PolygonMetrics> correlationRateList,
                                    List<Map<PureNumeratorDenominatorDto, Double>> avgCorrelationList) {
        var xAvgCorrelationMap = avgCorrelationList.get(0);
        var yAvgCorrelationMap = avgCorrelationList.get(1);
        for (PolygonMetrics currentRate : correlationRateList) {
            var xNum = currentRate.getX().getQuotient().get(0);
            var xDen = currentRate.getX().getQuotient().get(1);
            var yNum = currentRate.getY().getQuotient().get(0);
            var yDen = currentRate.getY().getQuotient().get(1);
            var numeratorDenominatorX = new PureNumeratorDenominatorDto(xNum, xDen);
            var numeratorDenominatorY = new PureNumeratorDenominatorDto(yNum, yDen);

            currentRate.setAvgCorrelationX(xAvgCorrelationMap.get(numeratorDenominatorX));
            currentRate.setAvgCorrelationY(yAvgCorrelationMap.get(numeratorDenominatorY));

            currentRate.setAvgMetricsX(xAvgCorrelationMap.get(numeratorDenominatorX));
            currentRate.setAvgMetricsY(yAvgCorrelationMap.get(numeratorDenominatorY));
        }
    }
}
