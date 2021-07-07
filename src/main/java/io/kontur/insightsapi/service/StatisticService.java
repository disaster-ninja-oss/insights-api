package io.kontur.insightsapi.service;

import io.kontur.insightsapi.model.BivariateStatistic;
import io.kontur.insightsapi.model.PolygonStatistic;
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

    public BivariateStatistic getBivariateStatistic() {
        return statisticRepository.getBivariateStatistic();
    }

    public PolygonStatistic getPolygonStatistic(){
        return new PolygonStatistic();
    }
}
