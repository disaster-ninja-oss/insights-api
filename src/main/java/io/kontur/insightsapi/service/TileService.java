package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.BivariateIndicatorDto;
import io.kontur.insightsapi.repository.TileRepository;
import lombok.RequiredArgsConstructor;
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

    @Value("${calculations.tiles.tile-size}")
    private Integer tileSize;

    @Value("${calculations.tiles.hex-edge-pixels}")
    private Integer hexEdgePixels;

    @Value("${calculations.tiles.max-h3-resolution}")
    private Integer maxH3Resolutions;

    @Value("${calculations.tiles.min-h3-resolution}")
    private Integer minH3Resolutions;

    @Value("${calculations.tiles.max-zoom}")
    private Integer maxZoom;

    @Value("${calculations.tiles.min-zoom}")
    private Integer minZoom;

    private final Logger logger = LoggerFactory.getLogger(TileService.class);

    private final TileRepository tileRepository;

    private final IndicatorService indicatorService;

    private final Map<Integer, Integer> zoomToH3Resolutions = new HashMap<>();;

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        List<BivariateIndicatorDto> indicators = switch (indicatorsClass) {
            case "all" -> indicatorService.getAllIndicators();
            case "general" -> indicatorService.getGeneralIndicators();
            default -> {
                String error = String.format("Tile indicator class is not defined. Class: %s", indicatorsClass);
                logger.error(error);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
            }
        };
        return tileRepository.getBivariateTileMvt(getResolution(z), z, x, y, indicators);
    }

    @PostConstruct
    private void postConstruct() {
        Map<Integer, Integer> res = tileRepository.initZoomToH3Resolutions(tileSize, hexEdgePixels, maxH3Resolutions,
                minH3Resolutions, maxZoom, minZoom);
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
