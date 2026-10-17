package fr.ensim.interop.introrest.model.weather;

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
public class ForecastItem {

	@JsonProperty("dt_txt")
	private String dateText;

	@JsonProperty("main")
	private Main main;

	@JsonProperty("weather")
	private List<Weather> weather;
}
