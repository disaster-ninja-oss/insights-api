package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BivariateIndicatorDto {

    @JsonProperty(value = "id", required = true)
    @NotEmpty(message = "Id cannot be null or empty")
    private String id;

    @JsonProperty(value = "label", required = true)
    @NotEmpty(message = "Label cannot be null or empty")
    private String label;

    @JsonProperty(value = "copyrights")
    private List<String> copyrights;

    @JsonProperty(value = "direction", required = true)
    @NotEmpty(message = "List of directions cannot be null or empty")
    private List<List<String>> direction;

    @JsonProperty(value = "isBase", required = true, defaultValue = "false")
    @NotNull
    private Boolean isBase = false;

    @JsonProperty(value = "isPublic", required = true, defaultValue = "false")
    @NotNull
    private Boolean isPublic = false;

    @JsonProperty(value = "allowedUsers")
    private List<String> allowedUsers;

}
