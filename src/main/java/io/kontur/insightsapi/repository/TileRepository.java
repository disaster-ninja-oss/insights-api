package io.kontur.insightsapi.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final QueryFactory queryFactory;

    @Value("classpath:/sql.queries/get_tile_mvt.sql")
    private Resource getTileMvtResource;

    public byte[] getTileMvt(Integer z, Integer x, Integer y){
        var paramSource = new MapSqlParameterSource("z", z);
        paramSource.addValue("x", x);
        paramSource.addValue("y", y);
        return namedParameterJdbcTemplate.queryForObject(queryFactory.getSql(getTileMvtResource), paramSource,
                (rs, rowNum) -> rs.getBytes("tile"));
    }
}
