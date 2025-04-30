package io.kontur.insightsapi.service;

import io.kontur.insightsapi.dto.AxisOverridesRequest;
import io.kontur.insightsapi.repository.AxisRepository;
import io.kontur.insightsapi.service.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AxisService {

    private final AxisRepository axisRepository;
    private final AuthService authService;

    public void insertOverrides(AxisOverridesRequest request) {
        String owner = authService.getCurrentUsername().orElseThrow();
        axisRepository.insertOverrides(request, owner);
    }

}
