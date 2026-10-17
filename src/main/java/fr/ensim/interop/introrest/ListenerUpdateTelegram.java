package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.joke.Joke;
import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Message;
import fr.ensim.interop.introrest.model.telegram.Update;
import fr.ensim.interop.introrest.model.weather.ForecastResponse;
import fr.ensim.interop.introrest.model.weather.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ListenerUpdateTelegram implements CommandLineRunner {

	@Value("${telegram.api.url}")
	private String telegramApiUrl;

	@Value("${telegram.bot.id}")
	private String botToken;

	@Value("${open.weather.api.url}")
	private String weatherApiUrl;

	@Value("${open.weather.api.token}")
	private String weatherApiToken;

	@Value("${ai.api.url}")
	private String aiApiUrl;

	@Value("${ai.api.token}")
	private String aiApiToken;

	private final RestTemplate restTemplate = new RestTemplate();
	private final Random random = new Random();
	private int offset = 0;

	private static final Logger logger = Logger.getLogger("ListenerUpdateTelegram");

	// Make JOKES mutable and manage IDs
	private static final List<Joke> JOKES = new ArrayList<>(Arrays.asList(
		new Joke(1, "Le programmeur optimiste", "Un programmeur dit à sa femme : 'Va faire les courses. Prends un litre de lait, et si ils ont des oeufs, prends-en 6.' Elle revient avec 6 litres de lait. 'Ils avaient des oeufs.'", 8.5),
		new Joke(2, "L'ascenseur", "Pourquoi les plongeurs plongent-ils toujours en arrière et jamais en avant ? Parce que sinon ils tomberaient dans le bateau.", 7.0),
		new Joke(3, "Le crocodile", "Qu'est-ce qu'un crocodile qui surveille des valises ? Un sac à dents.", 6.5),
		new Joke(4, "La bibliothèque", "Un homme entre dans une bibliothèque et demande : 'Vous avez des livres sur la paranoïa ?' La bibliothécaire chuchote : 'Ils sont juste derrière vous !'", 9.0),
		new Joke(5, "Le menuisier", "Qu'est-ce qu'un canif ? Un petit fien.", 5.0)
	));

	private static final AtomicInteger jokeIdCounter = new AtomicInteger(5); // Start after initial jokes
	@Override
	public void run(String... args) {
		logger.log(Level.INFO, "Démarage du listener d'updates Telegram...");

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					pollUpdates();
				} catch (Exception e) {
					logger.log(Level.WARNING, "Erreur lors du polling : " + e.getMessage());
				}
			}
		}, 0, 3000);
	}

	private void pollUpdates() {
		URI uri = UriComponentsBuilder
				.fromHttpUrl(telegramApiUrl + "/bot" + botToken + "/getUpdates")
				.queryParam("offset", offset)
				.build()
				.toUri();

		ApiResponseUpdateTelegram response = restTemplate.getForObject(uri, ApiResponseUpdateTelegram.class);

		if (response == null || !response.getOk() || response.getResult() == null) return;

		for (Update update : response.getResult()) {
			offset = update.getUpdateId() + 1;
			if (update.hasMessage() && update.getMessage().hasText()) {
				handleMessage(update.getMessage());
			}
		}
	}

	private void handleMessage(Message message) {
		String text = message.getText().toLowerCase();
		String chatId = String.valueOf(message.getChatId());
		Integer messageId = message.getMessageId();

		// On normalise le texte pour gérer avec ou sans "/"
		String cleanText = text.startsWith("/") ? text.substring(1) : text;

		try {
			if (cleanText.startsWith("addjoke ")) {
				sendMessage(chatId, addJoke(cleanText.substring(8)), messageId);
			} else if (cleanText.startsWith("getjoke ")) {
				sendMessage(chatId, getJokeDetails(cleanText.substring(8)), messageId);
			} else if (cleanText.startsWith("updatejoke ")) {
				sendMessage(chatId, updateJoke(cleanText.substring(11)), messageId);
			} else if (cleanText.startsWith("deletejoke ")) {
				sendMessage(chatId, deleteJoke(cleanText.substring(11)), messageId);
			} else if (text.contains("meteo") || text.contains("météo")) {
				boolean forecast = text.contains("forecast");
				String city = extractCity(text.replace("forecast", "").trim());
				sendMessage(chatId, forecast ? getForecastText(city) : getWeatherText(city), messageId);
			} else if (text.contains("blague")) {
				List<Joke> filtered = JOKES;
				if (text.contains("nulle") || text.contains("mauvaise")) {
					filtered = JOKES.stream().filter(j -> j.getNote() <= 6.5).collect(Collectors.toList());
				} else if (text.contains("bonne") || text.contains("excellente")) {
					filtered = JOKES.stream().filter(j -> j.getNote() >= 8.0).collect(Collectors.toList());
				}
				
				if (filtered.isEmpty()) filtered = JOKES;
				
				Joke joke = filtered.get(random.nextInt(filtered.size()));
				String jokeText = joke.getTitre() + "\n\n" + joke.getTexte() + "\n\nNote : " + joke.getNote() + "/10";
				sendMessage(chatId, jokeText, messageId);
			} else {
				sendMessage(chatId, askAi(message.getText()), messageId);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erreur inattendue lors du traitement du message : " + text, e);
			sendMessage(chatId, "⚠️ Désolé, une erreur interne est survenue lors du traitement de votre commande.", messageId);
		}
	}

	private String askAi(String prompt) {
		// Payload pour l'API Mistral AI (Format Chat Completions)
		Map<String, Object> message = new HashMap<>();
		message.put("role", "user");
		message.put("content", prompt);

		Map<String, Object> body = new HashMap<>();
		body.put("model", "open-mistral-7b");
		body.put("messages", Collections.singletonList(message));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(aiApiToken);

		try {
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
			Map<String, Object> response = restTemplate.postForObject(aiApiUrl, entity, Map.class);
			
			if (response != null && response.containsKey("choices")) {
				List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
				if (!choices.isEmpty()) {
					Map<String, Object> firstChoice = choices.get(0);
					Map<String, Object> messageResult = (Map<String, Object>) firstChoice.get("message");
					return (String) messageResult.get("content");
				}
			}
			return "🤖 Je n'ai pas reçu de réponse de l'IA.";
		} catch (Exception e) {
			logger.log(Level.WARNING, "Erreur lors de l'appel à l'IA : " + e.getMessage());
			return "🤖 Je n'ai pas pu formuler de réponse pour le moment.";
		}
	}

	private String extractCity(String text) {
		String[] parts = text.split("meteo|météo");
		if (parts.length > 1 && !parts[1].trim().isEmpty()) {
			return parts[1].trim();
		}
		return "Le Mans";
	}

	private String getWeatherText(String city) {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(weatherApiUrl + "/weather")
					.queryParam("q", city)
					.queryParam("appid", weatherApiToken)
					.queryParam("units", "metric")
					.queryParam("lang", "fr")
					.build()
					.toUri();

			WeatherResponse weather = restTemplate.getForObject(uri, WeatherResponse.class);

			if (weather != null && weather.getWeather() != null && !weather.getWeather().isEmpty()) {
				return String.format("Météo à %s : %s, %.1f°C",
						weather.getCity(),
						weather.getWeather().get(0).getDescription(),
						weather.getMain().getTemp());
			}
		} catch (Exception e) {
			return "Désolé, ville introuvable : " + city;
		}
		return "Météo indisponible.";
	}

	private String getForecastText(String city) {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(weatherApiUrl + "/forecast")
					.queryParam("q", city)
					.queryParam("appid", weatherApiToken)
					.queryParam("units", "metric")
					.queryParam("lang", "fr")
					.build()
					.toUri();

			ForecastResponse forecast = restTemplate.getForObject(uri, ForecastResponse.class);

			if (forecast == null || forecast.getList() == null || forecast.getList().isEmpty()) {
				return "Prévisions indisponibles pour " + city;
			}

			StringBuilder sb = new StringBuilder("Prévisions pour " + city + " :\n");
			forecast.getList().stream()
					.filter(item -> item.getDateText() != null && item.getDateText().contains("12:00:00"))
					.limit(2)
					.forEach(item -> sb.append(String.format("- %s : %s, %.1f°C\n",
							item.getDateText().substring(0, 10),
							item.getWeather().get(0).getDescription(),
							item.getMain().getTemp())));

			return sb.toString().trim();
		} catch (Exception e) {
			return "Désolé, ville introuvable : " + city;
		}
	}

	private void sendMessage(String chatId, String text, Integer replyToMessageId) {
		String url = telegramApiUrl + "/bot" + botToken + "/sendMessage";

		Map<String, Object> body = new HashMap<>();
		body.put("chat_id", chatId);
		body.put("text", text);
		if (replyToMessageId != null) {
			body.put("reply_to_message_id", replyToMessageId);
		}
		body.put("parse_mode", "Markdown"); // Enable Markdown for better formatting

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		try {
			restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
		} catch (HttpStatusCodeException e) {
			logger.log(Level.SEVERE, "Erreur API Telegram (HTTP " + e.getStatusCode() + ") pour le chat " + chatId + " : " + e.getResponseBodyAsString());
		} catch (RestClientException e) {
			logger.log(Level.SEVERE, "Erreur réseau lors de l'envoi du message à " + chatId, e);
		}
	}

	// --- Joke CRUD operations ---

	private String addJoke(String args) {
		String[] parts = args.split("\\|");
		if (parts.length != 3) {
			return "Format incorrect. Utilisez : `addjoke <titre>|<texte>|<note>`";
		}
		try {
			String titre = parts[0].trim();
			String texte = parts[1].trim();
			double note = Double.parseDouble(parts[2].trim());

			if (titre.isEmpty() || texte.isEmpty() || note < 0 || note > 10) {
				return "Titre et texte ne peuvent pas être vides, et la note doit être entre 0 et 10.";
			}

			int newId = jokeIdCounter.incrementAndGet();
			Joke newJoke = new Joke(newId, titre, texte, note);
			JOKES.add(newJoke);
			return String.format("Blague ajoutée avec succès ! ID : `%d`\nTitre : *%s*", newId, titre);
		} catch (NumberFormatException e) {
			return "La note doit être un nombre valide.";
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erreur lors de l'ajout de la blague : " + args, e);
			return "Une erreur est survenue lors de l'ajout de la blague.";
		}
	}

	private String getJokeDetails(String query) {
		if (StringUtils.isEmpty(query)) {
			return "Veuillez spécifier un ID ou un titre pour la blague. Ex: `getjoke 1` ou `getjoke programmeur`";
		}

		Joke foundJoke = null;
		try {
			int id = Integer.parseInt(query.trim());
			foundJoke = JOKES.stream().filter(j -> j.getId() == id).findFirst().orElse(null);
		} catch (NumberFormatException e) {
			// Not an ID, search by title
			String lowerCaseQuery = query.toLowerCase();
			foundJoke = JOKES.stream()
					.filter(j -> j.getTitre().toLowerCase().contains(lowerCaseQuery) || j.getTexte().toLowerCase().contains(lowerCaseQuery))
					.findFirst()
					.orElse(null);
		}

		if (foundJoke != null) {
			return String.format("ID : `%d`\nTitre : *%s*\n\nTexte : %s\n\nNote : %.1f/10",
					foundJoke.getId(), foundJoke.getTitre(), foundJoke.getTexte(), foundJoke.getNote());
		} else {
			return "Aucune blague trouvée pour : " + query;
		}
	}

	private String updateJoke(String args) {
		String[] parts = args.split("\\|");
		if (parts.length != 4) {
			return "Format incorrect. Utilisez : `updatejoke <id>|<nouveau_titre>|<nouveau_texte>|<nouvelle_note>`";
		}
		try {
			int id = Integer.parseInt(parts[0].trim());
			String newTitre = parts[1].trim();
			String newTexte = parts[2].trim();
			double newNote = Double.parseDouble(parts[3].trim());

			return JOKES.stream()
					.filter(j -> j.getId() == id)
					.findFirst()
					.map(joke -> {
						joke.setTitre(newTitre);
						joke.setTexte(newTexte);
						joke.setNote(newNote);
						return String.format("Blague ID `%d` mise à jour avec succès !", id);
					})
					.orElse("Blague avec l'ID `" + id + "` non trouvée.");
		} catch (NumberFormatException e) {
			return "L'ID et la note doivent être des nombres valides.";
		}
	}

	private String deleteJoke(String args) {
		try {
			int id = Integer.parseInt(args.trim());
			boolean removed = JOKES.removeIf(joke -> joke.getId() == id);
			if (removed) {
				return String.format("Blague ID `%d` supprimée avec succès !", id);
			} else {
				return "Blague avec l'ID `" + id + "` non trouvée.";
			}
		} catch (NumberFormatException e) {
			return "L'ID doit être un nombre valide. Ex: `deletejoke 1`";
		}
	}
}
