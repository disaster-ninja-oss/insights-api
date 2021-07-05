package io.kontur.insightsapi.service;

import io.kontur.insightsapi.model.PolygonStatistic;
import io.kontur.insightsapi.model.PopulationStatistic;
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

    public PolygonStatistic getPolygonStatistic() {
        return statisticRepository.getPolygonStatistic();
    }

    public PopulationStatistic getPopulationStatistic(){
        return new PopulationStatistic();
    }
}
