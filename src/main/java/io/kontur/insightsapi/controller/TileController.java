package io.kontur.insightsapi.controller;

import io.kontur.insightsapi.service.TileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tiles", description = "Tiles API")
@RestController
@RequestMapping("/tiles")
@RequiredArgsConstructor
public class TileController {

    private final TileService tileService;

    @Operation(summary = "Get bivariate mvt tile using z, x, y and indicator class.",
            tags = {"Tiles"},
            description = "Get bivariate mvt tile using z, x, y and indicator class.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/vnd.mapbox-vector-tile")),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Internal error")})
    @GetMapping(value = "/bivariate/v1/{z}/{x}/{y}.mvt", produces = "application/vnd.mapbox-vector-tile")
    public byte[] getBivariateTileMvt(@PathVariable Integer z, @PathVariable Integer x, @PathVariable Integer y,
                                      @RequestParam(defaultValue = "all") String indicatorsClass) {
        if (z < 0 || z > 8 || x < 0 || x > (Math.pow(2, z) - 1) || y < 0 || y > (Math.pow(2, z) - 1)) {
            return new byte[0];
        }
        return tileService.getBivariateTileMvt(z, x, y, indicatorsClass);
    }
}
