package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.AxisOverridesRequest;
import io.kontur.insightsapi.repository.AxisRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AxisService {

    private final AxisRepository axisRepository;

    public void insertOverrides(AxisOverridesRequest request) {
        axisRepository.insertOverrides(request);
    }

}
