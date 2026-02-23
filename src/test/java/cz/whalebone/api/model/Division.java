package cz.whalebone.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Division(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name
) {
}
