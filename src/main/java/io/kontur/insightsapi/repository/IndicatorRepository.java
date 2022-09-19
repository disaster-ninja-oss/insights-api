package io.kontur.insightsapi.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class IndicatorRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ObjectMapper objectMapper;

    @Transactional
    public String createIndicator(BivariateIndicatorDto bivariateIndicatorDto) throws JsonProcessingException {

        var paramSource = new MapSqlParameterSource()
                .addValue("id", bivariateIndicatorDto.getId())
                .addValue("label", bivariateIndicatorDto.getLabel())
                .addValue("copyrights", objectMapper.writeValueAsString(bivariateIndicatorDto.getCopyrights()))
                .addValue("direction", objectMapper.writeValueAsString(bivariateIndicatorDto.getDirection()))
                .addValue("isBase", bivariateIndicatorDto.getIsBase())
                .addValue("isPublic", bivariateIndicatorDto.getIsPublic())
                .addValue("allowedUsers", objectMapper.writeValueAsString(bivariateIndicatorDto.getAllowedUsers()));

        //TODO:add owner in future
        String queryBivariateIndicators = "INSERT INTO bivariate_indicators (param_id,param_label,copyrights,direction,is_base,param_uuid,owner,state,is_public,allowed_users,date) " +
                "VALUES (:id,:label,:copyrights::json,:direction::json,:isBase,gen_random_uuid(),null,'NEW',:isPublic,:allowedUsers,now()) RETURNING param_uuid;";
        return namedParameterJdbcTemplate.queryForObject(queryBivariateIndicators, paramSource, String.class);
    }
}
