package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.weather.ForecastItem;
import fr.ensim.interop.introrest.model.weather.ForecastResponse;
import fr.ensim.interop.introrest.model.weather.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class WeatherRestController {

	@Value("${open.weather.api.url}")
	private String weatherApiUrl;

	@Value("${open.weather.api.token}")
	private String weatherApiToken;

	private final RestTemplate restTemplate = new RestTemplate();

	@GetMapping("/weather")
	public WeatherResponse getWeather(
			@RequestParam("city") String city,
			@RequestParam(value = "forecast", required = false, defaultValue = "false") boolean forecast) {

		WeatherResponse response;
		try {
			response = restTemplate.getForObject(buildUrl("/weather", city), WeatherResponse.class);
		} catch (HttpClientErrorException e) {
			throw new ResponseStatusException(e.getStatusCode(), "Ville introuvable : " + city);
		}

		if (forecast && response != null) {
			try {
				ForecastResponse forecastResponse = restTemplate.getForObject(buildUrl("/forecast", city), ForecastResponse.class);
				if (forecastResponse != null && forecastResponse.getList() != null) {
					response.setForecast(filterNextTwoDays(forecastResponse.getList()));
				}
			} catch (HttpClientErrorException e) {
				throw new ResponseStatusException(e.getStatusCode(), "Erreur lors de la récupération des prévisions : " + city);
			}
		}

		return response;
	}

	private URI buildUrl(String endpoint, String city) {
		return UriComponentsBuilder.fromHttpUrl(weatherApiUrl + endpoint)
				.queryParam("q", city)
				.queryParam("appid", weatherApiToken)
				.queryParam("units", "metric")
				.queryParam("lang", "fr")
				.build()
				.toUri();
	}

	/**
	 * L'API /forecast renvoie 40 créneaux de 3h (5 jours). On garde un créneau par
	 * jour à midi pour les 2 prochains jours (donc on exclut aujourd'hui).
	 */
	private List<ForecastItem> filterNextTwoDays(List<ForecastItem> items) {
		String today = LocalDate.now().toString();
		return items.stream()
				.filter(item -> item.getDateText() != null
						&& item.getDateText().contains("12:00:00")
						&& !item.getDateText().startsWith(today))
				.limit(2)
				.collect(Collectors.toList());
	}
}
