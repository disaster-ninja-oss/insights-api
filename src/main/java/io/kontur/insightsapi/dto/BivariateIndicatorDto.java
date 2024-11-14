package io.kontur.insightsapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BivariateIndicatorDto {

    @JsonProperty(value = "id", required = true)
    @NotEmpty(message = "Id cannot be null or empty")
    @Deprecated
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
    @NotNull(message = "Incorrect type of is_base field, true/false expected.")
    private Boolean isBase = false;

    @JsonProperty(value = "uuid")
    private String externalId;

    @JsonIgnore
    private String internalId;

    @JsonProperty(value = "owner")
    private String owner;

    @JsonProperty(value = "state")
    private IndicatorState state;

    @JsonProperty(value = "isPublic", required = true, defaultValue = "false")
    @NotNull(message = "Incorrect type of is_public field, true/false expected.")
    private Boolean isPublic = false;

    @JsonProperty(value = "allowedUsers")
    private List<String> allowedUsers;

    @JsonProperty(value = "date")
    private OffsetDateTime date;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "coverage")
    private String coverage;

    @JsonProperty(value = "updateFrequency")
    private String updateFrequency;

    @JsonProperty(value = "application")
    private List<String> application;

    @JsonProperty(value = "unitId")
    private String unitId;

    @JsonProperty(value = "emoji")
    private String emoji;

    @JsonProperty(value = "lastUpdated")
    private OffsetDateTime lastUpdated;

    @JsonIgnore
    private String uploadId;

    private String md5Hash(String input) throws Exception {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Can't generate upload id");
        }
        byte[] hash = md.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getParamIdAndOwnerHash() throws Exception {
        // half of md5 string, so it fits with another 16 bytes of randomness in postgres application_name
        return md5Hash(id + "/" + owner).substring(0, 16);
    }

}
