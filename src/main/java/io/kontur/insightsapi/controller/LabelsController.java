package io.kontur.insightsapi.controller;

import io.kontur.insightsapi.dto.*;
import io.kontur.insightsapi.service.AxisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Indicators", description = "Indicators API")
@RestController
@RequestMapping("/indicators")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('uploadIndicators')")
public class LabelsController {

    private final AxisService axisService;

    @Operation(
            summary = "Create or update custom labels and stops for bivariate axis.",
            tags = {"Indicators"},
            description = "Provided numerator and denominator should exist as bivariate indicators for current owner. " +
                     "Accepts overrides for the following params: label, min, max, p25, p75. " +
                     "curl example: curl -w \":::\"%{http_code} http://localhost:8625/insights-api/indicators/axis/custom --header 'Authorization: Bearer %TOKEN%' --data '{\"numerator\":\"population\",\"denominator\":\"area_km2\",\"min\":0.0}'  -H \"Content-Type: application/json\"",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Internal error")})
    @PostMapping(value = "/axis/custom")
    public ResponseEntity<String> uploadLabels(@RequestBody AxisOverridesRequest request) {
        try {
            axisService.insertOverrides(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok().body("");
    }
}
