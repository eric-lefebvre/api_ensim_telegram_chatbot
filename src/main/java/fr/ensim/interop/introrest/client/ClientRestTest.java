package fr.ensim.interop.introrest.client;

import fr.ensim.interop.introrest.model.weather.WeatherResponse;
import org.springframework.web.client.RestTemplate;

public class ClientRestTest {

	public static void main(String[] args) {
		RestTemplate restTemplate = new RestTemplate();

		String token = "4187805ee7f57bf075f09446ff0d6e71";
		String city = "Paris";

		String url = "https://api.openweathermap.org/data/2.5/weather"
				+ "?q=" + city
				+ "&appid=" + token
				+ "&units=metric"
				+ "&lang=fr";

		WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

		System.out.println(response);
		System.out.println("Ville : " + response.getCity());
		System.out.println("Température : " + response.getMain().getTemp() + " °C");
		System.out.println("Météo : " + response.getWeather().get(0).getDescription());
	}
}
