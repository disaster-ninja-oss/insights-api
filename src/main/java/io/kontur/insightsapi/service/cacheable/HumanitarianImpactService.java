package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.dto.HumanitarianImpactDto;

import java.util.List;

public interface HumanitarianImpactService {

    List<HumanitarianImpactDto> calculateHumanitarianImpact(String geojson);
}
