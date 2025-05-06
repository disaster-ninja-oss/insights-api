package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.AdvancedAnalyticsQualitySortDto;
import io.kontur.insightsapi.dto.AdvancedAnalyticsRequest;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.BivariativeAxisDto;
import io.kontur.insightsapi.model.AdvancedAnalytics;
import io.kontur.insightsapi.model.AdvancedAnalyticsValues;
import io.kontur.insightsapi.service.cacheable.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AdvancedAnalyticsRepository implements AdvancedAnalyticsService {

    private final QueryFactory queryFactory;

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final Logger logger = LoggerFactory.getLogger(AdvancedAnalyticsRepository.class);

    private final int ADVANCED_ANALYTICS_TIMEOUT = 40;  // seconds

    private final Integer maxAnalyticsResolution = 8;

    @Value("classpath:/sql.queries/bivariate_axis.sql")
    private Resource bivariateAxis;

    @Value("classpath:/sql.queries/advanced_analytics_union.sql")
    private Resource advancedAnalyticsUnion;

    @Value("classpath:/sql.queries/advanced_analytics_world.sql")
    private Resource advancedAnalyticsWorld;

    @Value("classpath:/sql.queries/advanced_analytics_intersect.sql")
    private Resource advancedAnalyticsIntersect;

    @Value("classpath:/sql.queries/advanced_analytics_intersect_all_indicators.sql")
    private Resource advancedAnalyticsIntersectAllIndicators;

    @Value("classpath:/sql.queries/advanced_analytics_intersect_filtered_indicators.sql")
    private Resource advancedAnalyticsIntersectFilteredIndicators;

    @Transactional(readOnly = true)
    public List<BivariativeAxisDto> getBivariativeAxis() {
        return namedParameterJdbcTemplate.query(queryFactory.getSql(bivariateAxis),
                (rs, rowNum) -> BivariativeAxisDto.builder()
                .numerator(rs.getString(BivariateAxisColumns.numerator.name()))
                .denominator(rs.getString(BivariateAxisColumns.denominator.name()))
                .numeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()))
                .denominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name())).build());
    }

    @Transactional(readOnly = true)
    public List<BivariativeAxisDto> getFilteredBivariativeAxis(List<AdvancedAnalyticsRequest> argRequests) {
        String filterQuery = getBivariateAxisFilter(argRequests);
        return namedParameterJdbcTemplate.query(queryFactory.getSql(bivariateAxis) + filterQuery,
                (rs, rowNum) -> BivariativeAxisDto.builder()
                .numerator(rs.getString(BivariateAxisColumns.numerator.name()))
                .denominator(rs.getString(BivariateAxisColumns.denominator.name()))
                .numeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()))
                .denominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name())).build());
    }

    public String getQueryWithGeom(List<BivariativeAxisDto> argAxisDto) {
        List<String> bivariativeAxisDistinctList = argAxisDto.stream().flatMap(dto -> Stream.of(dto.getNumerator(), dto.getDenominator())).distinct().toList();

        return String.format(queryFactory.getSql(advancedAnalyticsIntersect), StringUtils.join(bivariativeAxisDistinctList, ","));
    }

    public String getUnionQuery(BivariativeAxisDto numDen) {
        return String.format(queryFactory.getSql(advancedAnalyticsUnion), numDen.getNumerator(), numDen.getDenominator());
    }

    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getWorldData() {
        List<BivariativeAxisDto> axisDtos = new ArrayList<>();
        List<List<AdvancedAnalyticsValues>> advancedAnalyticsValues = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(queryFactory.getSql(advancedAnalyticsWorld), (rs -> {
                BivariativeAxisDto bivariativeAxisDto = new BivariativeAxisDto();
                bivariativeAxisDto.setNumerator(rs.getString(BivariateAxisColumns.numerator.name()));
                bivariativeAxisDto.setDenominator(rs.getString(BivariateAxisColumns.denominator.name()));
                bivariativeAxisDto.setNumeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()));
                bivariativeAxisDto.setDenominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name()));
                axisDtos.add(bivariativeAxisDto);

                List<AdvancedAnalyticsValues> valuesList = createValuesList(rs);
                advancedAnalyticsValues.add(valuesList);
            }));
        } catch (Exception e) {
            String error = String.format("Can't get value from result set %s", e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }

        List<AdvancedAnalyticsQualitySortDto> qualitySortedList = createSortedList(axisDtos, advancedAnalyticsValues);
        return getAdvancedAnalyticsResult(qualitySortedList, axisDtos, advancedAnalyticsValues);
    }

    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getFilteredWorldData(List<AdvancedAnalyticsRequest> argRequests) {
        List<BivariativeAxisDto> axisDtos = new ArrayList<>();
        List<List<AdvancedAnalyticsValues>> advancedAnalyticsValues = new ArrayList<>();
        String filterQuery = getBivariateAxisFilter(argRequests);
        try {
            namedParameterJdbcTemplate.query(queryFactory.getSql(advancedAnalyticsWorld) + filterQuery, (rs -> {
                BivariativeAxisDto bivariativeAxisDto = new BivariativeAxisDto();
                bivariativeAxisDto.setNumerator(rs.getString(BivariateAxisColumns.numerator.name()));
                bivariativeAxisDto.setDenominator(rs.getString(BivariateAxisColumns.denominator.name()));
                bivariativeAxisDto.setNumeratorLabel(rs.getString(BivariateAxisColumns.numerator_label.name()));
                bivariativeAxisDto.setDenominatorLabel(rs.getString(BivariateAxisColumns.denominator_label.name()));
                axisDtos.add(bivariativeAxisDto);
                AdvancedAnalyticsRequest requestDto = argRequests.stream().filter(r ->
                                r.getNumerator().equals(bivariativeAxisDto.getNumerator()) && r.getDenominator().equals(bivariativeAxisDto.getDenominator()))
                        .findFirst().orElse(null);
                List<String> reqList = requestDto != null ? requestDto.getCalculations() : null;
                List<AdvancedAnalyticsValues> valuesList = createFilteredValuesList(rs, reqList);
                advancedAnalyticsValues.add(valuesList);
            }));
        } catch (Exception e) {
            String error = String.format("Can't get value from result set %s", e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }

        List<AdvancedAnalyticsQualitySortDto> qualitySortedList = createSortedList(axisDtos, advancedAnalyticsValues);
        return getAdvancedAnalyticsResult(qualitySortedList, axisDtos, advancedAnalyticsValues);
    }

    private String getBivariateAxisFilter(List<AdvancedAnalyticsRequest> argRequests) {
        List<String> filterQueryList = argRequests.stream().map(r -> "numerator='" + r.getNumerator() + "' and denominator='" + r.getDenominator() + "'").toList();
        return " where " + StringUtils.join(filterQueryList, " or ");
    }

    @Transactional(readOnly = true)
    public List<List<AdvancedAnalyticsValues>> getAdvancedAnalytics(String argQuery, String argGeometry) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", argGeometry);

        List<List<AdvancedAnalyticsValues>> result = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(argQuery, paramSource, (rs -> {
                result.add(createValuesList(rs));
            }));
        } catch (DataAccessResourceFailureException e) {
            String error = String.format(DatabaseUtil.ERROR_TIMEOUT, argGeometry);
            logger.error(error, e);
            throw new DataAccessResourceFailureException(error, e);
        } catch (EmptyResultDataAccessException e) {
            String error = String.format(DatabaseUtil.ERROR_EMPTY_RESULT, argGeometry);
            logger.error(error, e);
            throw new EmptyResultDataAccessException(error, 1);
        } catch (Exception e) {
            String error = String.format(DatabaseUtil.ERROR_SQL, argGeometry);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    @Override
    public List<AdvancedAnalytics> getAdvancedAnalyticsV2(String argGeometry, List<BivariateIndicatorDto> indicators) {
        int startResolution = 3;
        ExecutorService executor = Executors.newFixedThreadPool(maxAnalyticsResolution - startResolution + 1);
        // spawn 6 threads: each calculating advanced analytics for resolution 3..8
        List<Callable<Map<Integer, List<AdvancedAnalytics>>>> tasks = IntStream.rangeClosed(startResolution, maxAnalyticsResolution)
                .mapToObj(resolution -> createQueryTask(argGeometry, indicators, resolution))
                .collect(Collectors.toList());

        try {
            List<Future<Map<Integer, List<AdvancedAnalytics>>>> futures = executor
                    .invokeAll(tasks, ADVANCED_ANALYTICS_TIMEOUT, TimeUnit.SECONDS);

            // return result calculated for max resolution within timeout
            Optional<Map<Integer, List<AdvancedAnalytics>>> analytics = futures.stream()
                    .filter(Future::isDone)
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (InterruptedException | ExecutionException | CancellationException e) {
                            // the thread continues to live, but has a timeout state
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .max((r1, r2) -> {
                        // resolution comparator
                        int res1 = r1.keySet().iterator().next();
                        int res2 = r2.keySet().iterator().next();
                        return Integer.compare(res1, res2);
                    });

            if (analytics == null || analytics.get() == null) {
                logger.warn("no analytics result within timeout");
                return new ArrayList<>();
            }
            logger.info("return analytics for res {}", analytics.get().keySet().iterator().next());
            return analytics.get().values().iterator().next();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore the interrupted status
            return new ArrayList<>();
        } finally {
            executor.shutdown();
        }
    }

    private Callable<Map<Integer, List<AdvancedAnalytics>>> createQueryTask(String argGeometry, List<BivariateIndicatorDto> indicators, int resolution) {
        return () -> {
            List<AdvancedAnalytics> analytics = getAdvancedAnalyticsForResolution(argGeometry, indicators, resolution);
            Map<Integer, List<AdvancedAnalytics>> result = new HashMap<Integer, List<AdvancedAnalytics>>();
            result.put(resolution, analytics);
            return result;
        };
    }

    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getAdvancedAnalyticsForResolution(String argGeometry, List<BivariateIndicatorDto> indicators, Integer maxResolution) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", argGeometry);
        paramSource.addValue("max_resolution", maxResolution);

        List<String> uuids = DatabaseUtil.getUUIDs(indicators);
        String query = String.format(queryFactory.getSql(advancedAnalyticsIntersectAllIndicators), StringUtils.join(uuids, ", "));

        List<AdvancedAnalytics> result = new ArrayList<>();

        try {
            // NOTE: instead of dying by timeout we can save the result to redis - update cached result with higher resolution
            jdbcTemplate.execute(String.format("set statement_timeout='%ss'", ADVANCED_ANALYTICS_TIMEOUT + 1));
            namedParameterJdbcTemplate.query(query, paramSource, (rs -> {
                result.add(mapAdvancedAnalyticsWithAxis(rs, indicators, null));
            }));
            return result;
        } catch (DataAccessResourceFailureException e) {
            logger.warn(String.format("timeout calculating advancedAnalytics for resolution %s", maxResolution));
            String error = String.format(DatabaseUtil.ERROR_TIMEOUT, argGeometry);
            logger.error(error, e);
            throw new DataAccessResourceFailureException(error, e);
        } catch (EmptyResultDataAccessException e) {
            String error = String.format(DatabaseUtil.ERROR_EMPTY_RESULT, argGeometry);
            logger.error(error, e);
            throw new EmptyResultDataAccessException(error, 1);
        } catch (Exception e) {
            String error = String.format(DatabaseUtil.ERROR_SQL, argGeometry);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        } finally {
            jdbcTemplate.execute("reset statement_timeout");
        }
    }

    private AdvancedAnalytics mapAdvancedAnalyticsWithAxis(ResultSet rs, List<BivariateIndicatorDto> indicators, List<BivariativeAxisDto> axisDtos) {
        BivariateIndicatorDto numerator = indicators.stream()
                .filter(i -> i.getInternalId().equals(DatabaseUtil.getStringValueByColumnName(rs, "numerator_uuid")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Incorrect UUID of numerator"));
        BivariateIndicatorDto denominator = indicators.stream()
                .filter(i -> i.getInternalId().equals(DatabaseUtil.getStringValueByColumnName(rs, "denominator_uuid")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Incorrect UUID of denominator"));

        List<String> argCalculations = new ArrayList<>();

        if (axisDtos != null && !axisDtos.isEmpty()) {
            argCalculations = axisDtos.stream()
                    .filter(a -> a.getNumerator().equals(numerator.getId()) && a.getDenominator().equals(denominator.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No corresponding axis for returned pair of numerator uuid and denominator uuid"))
                    .getCalculations();
        }
        return AdvancedAnalytics.builder()
                .numerator(numerator.getId())
                .numeratorLabel(numerator.getLabel())
                .denominator(denominator.getId())
                .denominatorLabel(denominator.getLabel())
                .resolution(DatabaseUtil.getIntValueByColumnName(rs, "resolution"))
                .analytics(createFilteredValuesList(rs, argCalculations))
                .build();
    }

    @Transactional(readOnly = true)
    public List<List<AdvancedAnalyticsValues>> getFilteredAdvancedAnalytics(String argQuery, String argGeometry, List<BivariativeAxisDto> axisDtos) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", argGeometry);

        List<List<AdvancedAnalyticsValues>> result = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(argQuery, paramSource, (rs, rowNum) -> result.add(createFilteredValuesList(rs, axisDtos.get(rowNum).getCalculations())));
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s. Exception: %s", argGeometry, e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvancedAnalytics> getFilteredAdvancedAnalyticsV2(String argGeometry, List<BivariateIndicatorDto> indicators, List<BivariativeAxisDto> axisDtos) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue("polygon", argGeometry);

        List<String> uuidsAsPairsForRequest = new ArrayList<>();

        for (BivariativeAxisDto advancedAnalyticsRequest : axisDtos) {

            //TODO: add owner in future
            String pair = "('" +
                    indicators.stream()
                            .filter(i -> i.getId().equals(advancedAnalyticsRequest.getNumerator()))
                            .findFirst()
                            .orElseThrow(() -> new NoSuchElementException("Incorrect UUID of numerator"))
                            .getInternalId() +
                    "', '" +
                    indicators.stream()
                            .filter(i -> i.getId().equals(advancedAnalyticsRequest.getDenominator()))
                            .findFirst()
                            .orElseThrow(() -> new NoSuchElementException("Incorrect UUID of denominator"))
                            .getInternalId() +
                    "')";

            uuidsAsPairsForRequest.add(pair);
        }

        String query = String.format(queryFactory.getSql(advancedAnalyticsIntersectFilteredIndicators), StringUtils.join(uuidsAsPairsForRequest, ", "));

        List<AdvancedAnalytics> result = new ArrayList<>();
        try {
            namedParameterJdbcTemplate.query(query, paramSource, (rs -> {
                result.add(mapAdvancedAnalyticsWithAxis(rs, indicators, axisDtos));
            }));
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s. Exception: %s", argGeometry, e.getMessage());
            logger.error(error);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    private List<AdvancedAnalyticsValues> createFilteredValuesList(ResultSet rs, List<String> argCalculations) {
        //calculation list will be parametric, for now its constant
        List<String> calculationsList = Stream.of(Calculations.values())
                .map(Calculations::name)
                //no calculations field or array defined
                .filter(e -> {
                    if (argCalculations == null) {
                        return true;
                    } else if (!argCalculations.isEmpty()) {
                        return argCalculations.contains(e);
                    } else {
                        return true;
                    }
                })
                .toList();
        return calculationsList.stream().map(arg -> new AdvancedAnalyticsValues(arg,
                DatabaseUtil.getNullableDouble(rs, arg + "_value"),
                DatabaseUtil.getNullableDouble(rs, arg + "_quality"))
        ).toList();
    }

    private List<AdvancedAnalyticsValues> createValuesList(ResultSet rs) {
        //calculation list will be parametric, for now its constant
        List<String> calculationsList = Stream.of(Calculations.values()).map(Calculations::name).toList();
        return calculationsList.stream().map(arg -> new AdvancedAnalyticsValues(arg,
                DatabaseUtil.getNullableDouble(rs, arg + "_value"),
                DatabaseUtil.getNullableDouble(rs, arg + "_quality"))).toList();
    }

    private enum Calculations {
        sum, min, max, mean, stddev, median
    }

    private enum BivariateAxisColumns {
        numerator, denominator, numerator_label, denominator_label
    }

    public List<AdvancedAnalyticsQualitySortDto> createSortedList(List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues) {
        List<AdvancedAnalyticsQualitySortDto> returnList = new ArrayList<>();

        for (int i = 0; i < argAxis.size(); i++) {
            AdvancedAnalyticsQualitySortDto valuesWithMinQuality = new AdvancedAnalyticsQualitySortDto();
            valuesWithMinQuality.setNumerator(argAxis.get(i).getNumerator());
            valuesWithMinQuality.setDenominator(argAxis.get(i).getDenominator());
            List<AdvancedAnalyticsValues> advancedAnalyticsValues = argValues.get(i);
            Double minQuality = null;
            for (AdvancedAnalyticsValues value : advancedAnalyticsValues) {
                Double quality = value.getQuality();
                if (minQuality == null && quality != null) {
                    minQuality = Math.abs(quality);
                } else if (minQuality != null && quality != null && quality < minQuality) {
                    minQuality = Math.abs(quality);
                }
            }

            valuesWithMinQuality.setMinQuality(minQuality);
            returnList.add(valuesWithMinQuality);
        }
        return returnList.stream()
                .sorted(Comparator.comparing(AdvancedAnalyticsQualitySortDto::getMinQuality, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<AdvancedAnalytics> sortResultList(List<AdvancedAnalytics> unsortedResultList) {
        List<BivariativeAxisDto> argAxis = new ArrayList<>();
        List<List<AdvancedAnalyticsValues>> argValues = new ArrayList<>();
        List<Integer> resolutions = new ArrayList<>();

        for (AdvancedAnalytics analytics : unsortedResultList) {
            argAxis.add(BivariativeAxisDto
                    .builder()
                    .numerator(analytics.getNumerator())
                    .numeratorLabel(analytics.getNumeratorLabel())
                    .denominator(analytics.getDenominator())
                    .denominatorLabel(analytics.getDenominatorLabel())
                    .build());
            argValues.add(analytics.getAnalytics());
            resolutions.add(analytics.getResolution());
        }

        List<AdvancedAnalyticsQualitySortDto> argQualitySortedList = createSortedList(argAxis, argValues);

        return getAdvancedAnalyticsResult(argQualitySortedList, argAxis, argValues, resolutions);
    }

    public List<AdvancedAnalytics> getAdvancedAnalyticsResult(List<AdvancedAnalyticsQualitySortDto> argQualitySortedList, List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues) {
        // method overload: if resolution list is unknown, pass array of max res
        return getAdvancedAnalyticsResult(argQualitySortedList, argAxis, argValues, Collections.nCopies(argAxis.size(), maxAnalyticsResolution));
    }

    public List<AdvancedAnalytics> getAdvancedAnalyticsResult(List<AdvancedAnalyticsQualitySortDto> argQualitySortedList, List<BivariativeAxisDto> argAxis, List<List<AdvancedAnalyticsValues>> argValues, List<Integer> resolutions) {
        List<AdvancedAnalytics> returnList = new ArrayList<>();
        for (int i = 0; i < argAxis.size(); i++) {
            List<AdvancedAnalyticsValues> advancedAnalyticsValues = argValues.get(i);
            if (!advancedAnalyticsValues.isEmpty()) {
                AdvancedAnalytics analytics = new AdvancedAnalytics();
                analytics.setNumerator(argAxis.get(i).getNumerator());
                analytics.setDenominator(argAxis.get(i).getDenominator());
                analytics.setNumeratorLabel(argAxis.get(i).getNumeratorLabel());
                analytics.setDenominatorLabel(argAxis.get(i).getDenominatorLabel());
                analytics.setResolution(resolutions.get(i));
                analytics.setAnalytics(advancedAnalyticsValues);
                //get order according to quality value
                analytics.setOrder(getIndexOfListByQuality(argQualitySortedList, argAxis.get(i).getNumerator(), argAxis.get(i).getDenominator()));
                returnList.add(analytics);
            }
        }
        //sort by quality order
        return returnList.stream()
                .sorted(Comparator.comparing(AdvancedAnalytics::getOrder))
                .toList();
    }

    private Integer getIndexOfListByQuality(List<AdvancedAnalyticsQualitySortDto> argQualitySortedList, String argNumerator, String argDenominator) {
        OptionalInt indexOpt = IntStream.range(0, argQualitySortedList.size())
                .filter(i -> argNumerator.equals(argQualitySortedList.get(i).getNumerator()) && argDenominator.equals(argQualitySortedList.get(i).getDenominator()))
                .findFirst();

        if (indexOpt.isPresent()) {
            return indexOpt.getAsInt();
        }
        return 0;
    }
}
