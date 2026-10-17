package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.telegram.ApiResponseTelegram;
import fr.ensim.interop.introrest.model.telegram.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
public class MessageRestController {

	@Value("${telegram.api.url}")
	private String telegramApiUrl;

	@Value("${telegram.bot.id}")
	private String botToken;

	@Value("${telegram.chat.id}")
	private String defaultChatId;

	private final RestTemplate restTemplate = new RestTemplate();

	@PostMapping("/message")
	public ApiResponseTelegram<Message> sendMessage(
			@RequestParam(required = false) String chatId,
			@RequestParam String text) {

		if (chatId == null) chatId = defaultChatId;

		URI uri = UriComponentsBuilder
				.fromHttpUrl(telegramApiUrl + "/bot" + botToken + "/sendMessage")
				.queryParam("chat_id", chatId)
				.queryParam("text", text)
				.build()
				.toUri();

		return restTemplate.exchange(
				uri,
				HttpMethod.POST,
				null,
				new ParameterizedTypeReference<ApiResponseTelegram<Message>>() {}
		).getBody();
	}
}
