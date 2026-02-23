package cz.whalebone.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Team(
        @JsonProperty("name") String name,
        @JsonProperty("location") String location,
        @JsonProperty("founded") int founded,
        @JsonProperty("firstYearOfPlay") int firstYearOfPlay,
        @JsonProperty("division") Division division,
        @JsonProperty("officialSiteUrl") String officialSiteUrl
) {
}
