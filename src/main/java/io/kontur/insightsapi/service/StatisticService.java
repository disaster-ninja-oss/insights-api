package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.PolygonStatisticRequest;
import io.kontur.insightsapi.model.Statistic;
import io.kontur.insightsapi.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

    public Statistic getAllStatistic() {
        return statisticRepository.getAllStatistic();
    }

    public Statistic getPolygonStatistic(PolygonStatisticRequest request){
        return statisticRepository.getPolygonStatistic(request);
    }
}
