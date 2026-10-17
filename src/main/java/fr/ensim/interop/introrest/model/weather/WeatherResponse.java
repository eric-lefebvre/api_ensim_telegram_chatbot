package fr.ensim.interop.introrest.model.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {

	@JsonProperty("name")
	private String city;

	@JsonProperty("weather")
	private List<Weather> weather;

	@JsonProperty("main")
	private Main main;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<ForecastItem> forecast;
}
