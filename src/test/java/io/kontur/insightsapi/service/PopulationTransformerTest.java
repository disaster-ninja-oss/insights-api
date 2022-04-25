package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.CalculatePopulationDto;
import io.kontur.insightsapi.dto.StatisticDto;
import io.kontur.insightsapi.repository.PopulationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PopulationTransformerTest {

    private static final String POPULATION_QUERY = "POLYGON((0 0,0 5,5 5,5 0,0 0))";

    @Test
    void calculatePopulation() {
        PopulationRepository populationRepository = mock(PopulationRepository.class);
        PopulationTransformer populationTransformer = new PopulationTransformer(populationRepository);
        when(populationRepository.getPopulationAndGdp(POPULATION_QUERY)).thenReturn(getPopulation());
        when(populationRepository.getArea(POPULATION_QUERY)).thenReturn(BigDecimal.ONE);

        StatisticDto statistic = populationTransformer.calculatePopulation(POPULATION_QUERY);
        Assertions.assertNotNull(statistic, "Population statistic is not received");
        Map<String, CalculatePopulationDto> population = getPopulation();

        Assertions.assertEquals(population.values().iterator().next().getPopulation(), statistic.getPopulation());
        Assertions.assertEquals(population.values().iterator().next().getUrban(), statistic.getUrban());
        Assertions.assertEquals(population.values().iterator().next().getGdp(), statistic.getGdp());

    }

    private Map<String, CalculatePopulationDto> getPopulation() {
        Map<String, CalculatePopulationDto> populationMap = new HashMap<>();
        CalculatePopulationDto dto = new CalculatePopulationDto();
        dto.setPopulation(BigDecimal.valueOf(1000));
        dto.setUrban(BigDecimal.valueOf(500));
        dto.setGdp(BigDecimal.valueOf(10000));
        dto.setType("population");
        populationMap.put(dto.getType(), dto);
        return populationMap;
    }

    @Test
    void test(){
        System.out.println(Pattern.matches("(\\d|\\w){1,255}", "123asdasd123-asdfadsf"));
    }
}