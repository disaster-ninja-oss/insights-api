package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.CalculatePopulationDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PopulationTransformerIT {

    private static final String POPULATION_QUERY = "POLYGON((0 0,0 5,5 5,5 0,0 0))";

    @Autowired
    private PopulationTransformer populationTransformer;

    @Disabled("will be fixed after adding postgis h3index type to test image")
    @Test
    void calculatePopulationAndGdp() {
        Optional<Map<String, CalculatePopulationDto>> population = populationTransformer.calculatePopulationAndGdp(POPULATION_QUERY);
        Assertions.assertTrue(population.isPresent(), "Population is not received");
        Assertions.assertTrue(population.get().size() >= 1);
        Assertions.assertEquals(BigDecimal.valueOf(700), population.get().values().iterator().next().getPopulation());
        Assertions.assertEquals(BigDecimal.valueOf(400), population.get().values().iterator().next().getUrban());
        Assertions.assertEquals(BigDecimal.valueOf(900000), population.get().values().iterator().next().getGdp());
        Assertions.assertEquals("population", population.get().values().iterator().next().getType());
    }

    @Test
    void calculateArea() {
        BigDecimal area = populationTransformer.calculateArea(POPULATION_QUERY);
        Assertions.assertNotNull(area, "Area is not received");
        Assertions.assertTrue(area.compareTo(BigDecimal.ZERO) > 0);
    }
}