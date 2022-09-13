package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BivariateIndicatorDto {

    @JsonProperty(value = "id", required = true)
    @NotNull
    private String id;

    @JsonProperty(value = "label", required = true)
    @NotNull
    private String label;

    @JsonProperty(value = "copyrights")
    private List<String> copyrights = new ArrayList<>();

    @JsonProperty(value = "direction", required = true)
    private List<List<String>> direction = new ArrayList<>();

    @JsonProperty(value = "isBase", required = true)
    private Boolean isBase = false;

    @JsonProperty(value = "isPublic", required = true)
    private Boolean isPublic = false;

    @JsonProperty(value = "allowedUsers")
    private List<String> allowedUsers = new ArrayList<>();

}
