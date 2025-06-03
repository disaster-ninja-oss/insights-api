package io.kontur.insightsapi.repository;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.dto.FunctionArgs;
import io.kontur.insightsapi.model.FunctionResult;
import io.kontur.insightsapi.model.Unit;
import io.kontur.insightsapi.service.cacheable.FunctionsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FunctionsRepository implements FunctionsService {

    @Value("classpath:/sql.queries/function_intersect_v2.sql")
    private Resource functionIntersectV2;

    private static final Pattern VALID_STRING_PATTERN = Pattern.compile("(\\d|\\w){1,255}");

    private final Logger logger = LoggerFactory.getLogger(FunctionsRepository.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final QueryFactory queryFactory;

    private final IndicatorRepository indicatorRepository;

    public List<FunctionResult> calculateFunctionsResult(String geojson, List<FunctionArgs> args) {
        var paramSource = new MapSqlParameterSource("polygon", geojson);
        List<FunctionResult> result = new ArrayList<>();
        String query = getFunctionsQuery(args);
        try {
            namedParameterJdbcTemplate.query(query, paramSource, (rs -> {
                result.addAll(createFunctionResultList(args, rs));
            }));
        } catch (Exception e) {
            String error = String.format("Sql exception for geometry %s", geojson);
            logger.error(error, e);
            throw new IllegalArgumentException(error, e);
        }
        return result;
    }

    public String getFunctionsQuery(List<FunctionArgs> args) {
        // Build list of SQL expressions for requested functions. Unsupported
        // functions are filtered out to avoid malformed SQL with dangling commas.
        List<String> params = args.stream()
                .map(this::createFunctionsForSelect)
                .filter(Objects::nonNull)
                .toList();

        if (params.isEmpty()) {
            logger.error("No valid functions provided for query");
            throw new IllegalArgumentException("Empty functions list");
        }

        List<String> paramIds = args.stream()
                .flatMap(arg -> Stream.of(arg.getX(), arg.getY()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<BivariateIndicatorDto> bivariateIndicatorDtos = indicatorRepository.getSelectedBivariateIndicators(paramIds);
        // TODO: move to indicatorRepository.getSelectedBivariateIndicators?
        List<String> existingIndicatorIds = bivariateIndicatorDtos.stream()
                .map(i -> i.getId())
                .toList();
        
        paramIds.stream()
                .filter(p -> !existingIndicatorIds.contains(p))
                .findFirst()
                .ifPresent(p -> {
                    logger.warn("No such indicator: " + p);
                    throw new EmptyResultDataAccessException("No such indicator: " + p, 1);
                });
       
        String CTE = DatabaseUtil.buildCTE("8", bivariateIndicatorDtos, "", false);
        return String.format(queryFactory.getSql(functionIntersectV2), CTE, StringUtils.join(params, ", "));
    }

    private String createFunctionsForSelect(FunctionArgs functionArgs) {
        String validId = checkString(functionArgs.getId());
        String validX = checkString(functionArgs.getX());
        String validY = checkString(functionArgs.getY());
        return switch (functionArgs.getName()) {
            case "sumX" -> "sum(" + validX + ") as result" + validId;
            case "sumXWhereNoY" -> "sum(" + validX + " * (1 - sign(coalesce(" + validY + ", 0)))) " +
                    "as result" + validId;
            case "percentageXWhereNoY" -> "(sum(" + validX + " * (1 - sign(coalesce(" + validY + ", 0))))/sum(" +
                    validX + ") filter (where " + validX + " != 0 and " + validX + " is not null)) * 100 as result" + validId;
            case "maxX" -> "max(" + validX + ") as result" + validId;
            case "minX" -> "min(" + validX + ") as result" + validId;
            case "avgX" -> "(avg(" + validX + ") filter (where " + validX + " != 0 and " + validX + " is not null)) as result" + validId;
            case "countX" -> "count(" + validX + ") as result" + validId;
            default -> {
                logger.warn("Unsupported function name: {}", functionArgs.getName());
                yield null;
            }
        };
    }

    private List<FunctionResult> createFunctionResultList(List<FunctionArgs> args, ResultSet rs) {
        return args.stream().map(arg -> {
            try {
                //TODO: in future getLabel should consume UUID
                var result = rs.getBigDecimal("result" + arg.getId());  // bug #18327
                return new FunctionResult(arg.getId(), result == null ? BigDecimal.ZERO : result, getUnit(arg), getLabel(arg.getX()), getLabel(arg.getY()));
            } catch (SQLException e) {
                logger.error("Can't get BigDecimal value from result set", e);
                return null;
            }
        }).toList();
    }

    //TODO: change deprecated method to 'getIndicatorByUuid' after transition from param_ir to param_uuid as indicator identifier
    private String getLabel(String paramId) {
        return Strings.isEmpty(paramId) ? null : indicatorRepository.getLabelByParamId(paramId);
    }

    private Unit getUnit(FunctionArgs arg) {
        String query;
        //TODO: localization for units can be added to this request in future
        try {
            if ("percentageXWhereNoY".equals(arg.getName())) {
                query = """
                        select unit_id, short_name, long_name
                        from bivariate_unit_localization
                        where unit_id = 'perc'
                        """;
            } else {
                query = String.format("""
                        select bivariate_unit_localization.unit_id, short_name, long_name
                        from bivariate_indicators_metadata bi
                        left join bivariate_unit_localization on bi.unit_id = bivariate_unit_localization.unit_id
                        where bi.param_id = '%s' limit 1;
                        """, arg.getX());
            }
            return jdbcTemplate.queryForObject(query, (rs, rowNum) ->
                    Unit.builder()
                            .id(rs.getString("unit_id"))
                            .shortName(rs.getString("short_name"))
                            .longName(rs.getString("long_name"))
                            .build());
        } catch (EmptyResultDataAccessException e) {
            logger.error("No such indicator: " + arg.getX(), e);
            throw new EmptyResultDataAccessException("No such indicator: " + arg.getX(), 1);
        }
    }

    private String checkString(String string) {
        if (string != null && !VALID_STRING_PATTERN.matcher(string).matches()) {
            logger.error("Illegal argument for request creation was found");
            throw new IllegalArgumentException("Illegal argument for request creation was found");
        }
        return string;
    }
}
