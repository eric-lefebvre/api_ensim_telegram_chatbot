package fr.ensim.interop.introrest.model.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Main {

	@JsonProperty("temp")
	private Double temp;

	@JsonProperty("feels_like")
	private Double feelsLike;

	@JsonProperty("humidity")
	private Integer humidity;
}
