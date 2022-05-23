package io.kontur.insightsapi.controller;

import io.kontur.insightsapi.repository.TileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tiles", description = "Tiles API")
@RestController
@RequestMapping("/tiles")
@RequiredArgsConstructor
public class TileController {

    private final TileRepository tileRepository;

    @Operation(summary = "Get tile using z, x, y.",
            tags = {"Tiles"},
            description = "Get tile using z, x, y.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(mediaType = "application/vnd.mapbox-vector-tile")),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Internal error")})
    @GetMapping(value = "/{z}/{x}/{y}.mvt", produces = "application/vnd.mapbox-vector-tile")
    public byte[] getTileMvt(@PathVariable Integer z, @PathVariable Integer x, @PathVariable Integer y) {
        if (z < 0 || z > 8 || x < 0 || x > (Math.pow(2, z) - 1) || y < 0 || y > (Math.pow(2, z) - 1)) {
            return new byte[0];
        }
        return tileRepository.getTileMvt(z, x, y);
    }
}
