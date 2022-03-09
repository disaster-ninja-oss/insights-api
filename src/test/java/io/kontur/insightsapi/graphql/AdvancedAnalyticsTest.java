package io.kontur.insightsapi.graphql;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;

@Disabled("Just for local debugging")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AdvancedAnalyticsTest.Config.class)
@TestPropertySource(properties = {
        "url.local=http://localhost:8625/insights-api/graphql",
        "url.dev=https://test-apps02.konturlabs.com/insights-api/graphql"
})
public class AdvancedAnalyticsTest {

    @Value("${url.local}")
    String URL_LOCAL;

    @Value("${url.dev}")
    String URL_DEV;

    String JSON_QUERY = String.format("""
              {
              polygonStatistic(polygonStatisticRequest: {polygon: "{\\"type\\":\\"FeatureCollection\\",\\"features\\":[{\\"type\\":\\"Feature\\",\\"geometry\\":{\\"type\\":\\"Polygon\\",\\"coordinates\\":[[[121.33,0.049],[121.33,0.081],[121.328,0.112],[121.325,0.143],[121.322,0.175],[121.317,0.206],[121.311,0.237],[121.304,0.267],[121.295,0.298],[121.286,0.328],[121.276,0.357],[121.265,0.387],[121.252,0.416],[121.239,0.444],[121.225,0.472],[121.21,0.5],[121.193,0.527],[121.176,0.553],[121.158,0.579],[121.139,0.604],[121.12,0.628],[121.099,0.652],[121.077,0.675],[121.055,0.697],[121.032,0.719],[121.008,0.739],[120.984,0.759],[120.959,0.778],[120.933,0.796],[120.907,0.813],[120.88,0.83],[120.852,0.845],[120.824,0.859],[120.796,0.872],[120.767,0.885],[120.738,0.896],[120.708,0.906],[120.678,0.915],[120.647,0.923],[120.617,0.931],[120.586,0.937],[120.555,0.941],[120.524,0.945],[120.492,0.948],[120.461,0.95],[120.429,0.95],[120.398,0.95],[120.367,0.948],[120.335,0.945],[120.304,0.941],[120.273,0.937],[120.242,0.931],[120.211,0.923],[120.181,0.915],[120.151,0.906],[120.121,0.896],[120.092,0.885],[120.063,0.872],[120.034,0.859],[120.006,0.845],[119.979,0.83],[119.952,0.813],[119.926,0.796],[119.9,0.778],[119.875,0.759],[119.85,0.739],[119.827,0.719],[119.804,0.697],[119.781,0.675],[119.76,0.652],[119.739,0.628],[119.719,0.604],[119.701,0.579],[119.683,0.553],[119.665,0.527],[119.649,0.5],[119.634,0.472],[119.62,0.444],[119.606,0.416],[119.594,0.387],[119.583,0.357],[119.573,0.328],[119.563,0.298],[119.555,0.267],[119.548,0.237],[119.542,0.206],[119.537,0.175],[119.533,0.143],[119.531,0.112],[119.529,0.081],[119.528,0.049],[119.529,0.018],[119.531,-0.014],[119.533,-0.045],[119.537,-0.076],[119.542,-0.107],[119.548,-0.138],[119.555,-0.169],[119.563,-0.199],[119.573,-0.229],[119.583,-0.259],[119.594,-0.288],[119.606,-0.317],[119.62,-0.346],[119.634,-0.374],[119.649,-0.401],[119.665,-0.428],[119.683,-0.454],[119.701,-0.48],[119.719,-0.505],[119.739,-0.53],[119.76,-0.554],[119.781,-0.577],[119.804,-0.599],[119.827,-0.62],[119.85,-0.641],[119.875,-0.661],[119.9,-0.68],[119.926,-0.698],[119.952,-0.715],[119.979,-0.731],[120.006,-0.746],[120.034,-0.76],[120.063,-0.774],[120.092,-0.786],[120.121,-0.797],[120.151,-0.808],[120.181,-0.817],[120.211,-0.825],[120.242,-0.832],[120.273,-0.838],[120.304,-0.843],[120.335,-0.847],[120.367,-0.849],[120.398,-0.851],[120.429,-0.852],[120.461,-0.851],[120.492,-0.849],[120.524,-0.847],[120.555,-0.843],[120.586,-0.838],[120.617,-0.832],[120.647,-0.825],[120.678,-0.817],[120.708,-0.808],[120.738,-0.797],[120.767,-0.786],[120.796,-0.774],[120.824,-0.76],[120.852,-0.746],[120.88,-0.731],[120.907,-0.715],[120.933,-0.698],[120.959,-0.68],[120.984,-0.661],[121.008,-0.641],[121.032,-0.62],[121.055,-0.599],[121.077,-0.577],[121.099,-0.554],[121.12,-0.53],[121.139,-0.505],[121.158,-0.48],[121.176,-0.454],[121.193,-0.428],[121.21,-0.401],[121.225,-0.374],[121.239,-0.346],[121.252,-0.317],[121.265,-0.288],[121.276,-0.259],[121.286,-0.229],[121.295,-0.199],[121.304,-0.169],[121.311,-0.138],[121.317,-0.107],[121.322,-0.076],[121.325,-0.045],[121.328,-0.014],[121.33,0.018],[121.33,0.049],[121.33,0.049]]]},\\"properties\\":{}}]}"}) {
                analytics {
                  advancedAnalytics {
                    numerator
                    denominator
                    numeratorLabel
                    denominatorLabel
                    analytics {
                      value
                      calculation
                      quality
                    }
                  }
                }
              }
            }
            """).trim();

    @Test
    public void testGraphQlResultLocal() {
        LinkedHashMap<String, Object> response = getRestResult(URL_LOCAL);
        runTests(response);
    }

    @Test
    public void testGraphQlResultDev() {
        LinkedHashMap<String, Object> response = getRestResult(URL_DEV);
        runTests(response);
    }

    LinkedHashMap<String, Object> getRestResult(String argUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/graphql"); // maintain graphql
        ResponseEntity<LinkedHashMap<String, Object>> rateResponse =
                restTemplate.exchange(argUrl,
                        HttpMethod.POST, new HttpEntity<>(JSON_QUERY, headers), new ParameterizedTypeReference<>() {
                        });
        return rateResponse.getBody();
    }

    public void runTests(LinkedHashMap<String, Object> argResponse) {
        Assertions.assertNotNull(argResponse, "response should not be null");

        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) argResponse.get("data");
        Assertions.assertNotNull(data, "data should not be null");

        LinkedHashMap<String, Object> polygonStatistic = (LinkedHashMap<String, Object>) data.get("polygonStatistic");
        Assertions.assertNotNull(polygonStatistic, "polygonStatistic should not be null");

        LinkedHashMap<String, Object> analytics = (LinkedHashMap<String, Object>) polygonStatistic.get("analytics");
        Assertions.assertNotNull(analytics, "analytics should not be null");

        List<LinkedHashMap<String, Object>> advancedAnalytics = (List<LinkedHashMap<String, Object>>) analytics.get("advancedAnalytics");
        Assertions.assertNotNull(advancedAnalytics, "advancedAnalytics should not be null");
        Assertions.assertTrue(advancedAnalytics.size() > 0, "advancedAnalytics size should be > 0");

        Assertions.assertNotNull(advancedAnalytics.get(0), "advancedAnalytics should have one element");
        LinkedHashMap<String, Object> analytic = advancedAnalytics.get(0);
        Assertions.assertNotNull(analytic.get("numerator"), "numerator should exist");
        Assertions.assertNotNull(analytic.get("denominator"), "denominator should exist");
        Assertions.assertNotNull(analytic.get("numeratorLabel"), "numeratorLabel should exist");
        Assertions.assertNotNull(analytic.get("denominatorLabel"), "denominatorLabel should exist");

        List<LinkedHashMap<String, Object>> analyticsValues = (List<LinkedHashMap<String, Object>>) analytic.get("analytics");
        Assertions.assertNotNull(analyticsValues, "analyticsValues should not be null");
        Assertions.assertTrue(analyticsValues.size() > 0, "analyticsValues size should be > 0");

        Assertions.assertNotNull(analyticsValues.get(0), "analyticsValues should have one element");
        LinkedHashMap<String, Object> calculations = analyticsValues.get(0);
        Assertions.assertNotNull(calculations.get("value"));
        Assertions.assertNotNull(calculations.get("calculation"));
        Assertions.assertNotNull(calculations.get("quality"));
    }

    @Configuration
    static class Config {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}