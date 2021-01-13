package io.kontur.insightsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

    private final GeometryTransformer geometryTransformer;

    public Statistic getAllStatistic() {
        return statisticRepository.getAllStatistic();
    }

    public Statistic getPolygonStatistic(PolygonStatisticRequest request) throws JsonProcessingException {
        var transformedGeometry = geometryTransformer.transform(request.getPolygon());
        return statisticRepository.getPolygonStatistic(PolygonStatisticRequest.builder()
                .polygon(transformedGeometry)
                .xNumeratorList(request.getXNumeratorList())
                .yNumeratorList(request.getYNumeratorList())
                .build());
    }
}
