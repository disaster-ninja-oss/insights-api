package io.kontur.insightsapi.service;

import io.kontur.insightsapi.repository.TileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TileService {

    private final TileRepository tileRepository;

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y) {
        var bivariateIndicators = tileRepository.getBivariateIndicators();
        return tileRepository.getBivariateTileMvt(z, x, y, bivariateIndicators);
    }

}
