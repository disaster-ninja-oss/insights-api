package io.kontur.insightsapi.service;

import io.kontur.insightsapi.repository.TileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class TileService {

    @Value("${calculations.useStatSeparateTables:false}")
    private Boolean useStatSeparateTables;

    @Value("${calculations.tiles.max-zoom}")
    private Integer maxZoom;

    @Value("${calculations.tiles.min-zoom}")
    private Integer minZoom;

    private final Logger logger = LoggerFactory.getLogger(TileService.class);

    private final TileRepository tileRepository;

    private final Map<Integer, Integer> zoomToH3Resolutions = new HashMap<>();;

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass, List<String> indicators) {
        List<String> bivariateIndicators;
        switch (indicatorsClass) {
            case ("all") -> bivariateIndicators = tileRepository.getAllBivariateIndicators(false, indicators);
            case ("general") -> bivariateIndicators = tileRepository.getAllBivariateIndicators(true, indicators);
            default -> {
                String error = String.format("Tile indicator class is not defined. Class: %s", indicatorsClass);
                logger.error(error);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
            }
        }
        Integer h3res = getResolution(z);
        if (indicators != null && h3res > 8) {
            // for paid population tiles api limit resolution to 8
            h3res = 8;
        }
        return tileRepository.getBivariateTileMvt(h3res, z, x, y, bivariateIndicators);
    }

    public byte[] getBivariateTileMvtIndicatorsList(Integer z, Integer x, Integer y, List<String> indicatorsList) {
        var indicators = indicatorsList;
        if (CollectionUtils.isEmpty(indicators)) {
            indicators = tileRepository.getAllBivariateIndicators(false, null);
        } else {
            if (!checkIndicatorsList(indicators)) {
                String error = "Wrong indicator name. " +
                        "All indicators should be from bivariate_indicators table";
                logger.error(error);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
            }
        }
        Integer resolution = getResolution(z);
        if (useStatSeparateTables) {
            return tileRepository.getBivariateTileMvtIndicatorsListV2(resolution, z, x, y, indicators);
        }
        return tileRepository.getBivariateTileMvt(resolution, z, x, y, indicators);
    }

    private boolean checkIndicatorsList(List<String> indicatorsList) {
        var bivariateIndicators = tileRepository.getAllBivariateIndicators(false, null);
        return bivariateIndicators.containsAll(indicatorsList);
    }

    @PostConstruct
    private void postConstruct() {
        Map<Integer, Integer> res = tileRepository.initZoomToH3Resolutions();
        if (MapUtils.isNotEmpty(res)) {
            zoomToH3Resolutions.putAll(res);
        }
        if (!zoomToH3Resolutions.containsKey(minZoom)) {
            zoomToH3Resolutions.put(minZoom, minZoom);
        }
        if (!zoomToH3Resolutions.containsKey(maxZoom)) {
            zoomToH3Resolutions.put(maxZoom, maxZoom);
        }
    }
    private Integer getResolution(Integer z) {
        return zoomToH3Resolutions.containsKey(z)
                ? zoomToH3Resolutions.get(z)
                : z < minZoom
                    ? zoomToH3Resolutions.get(minZoom)
                    : z > maxZoom
                        ? zoomToH3Resolutions.get(maxZoom)
                        : z;
    }
}
