# ChatBot Telegram – Spring Boot (TP Interop ENSIM 2022)

Bot Telegram en Java/Spring Boot qui agrège plusieurs API REST : météo (OpenWeatherMap), blagues (CRUD en mémoire) et IA conversationnelle (Mistral AI).

## Stack technique
- Java 8, Spring Boot 2.4.5 (`spring-boot-starter-web`, `actuator`), Maven, Lombok, Jackson
- Appels HTTP sortants via `RestTemplate` + `UriComponentsBuilder` ; JSON désérialisé dans des POJO (`model/telegram`, `model/weather`, `model/joke`)

## Fonctionnement du bot
`ListenerUpdateTelegram` (`CommandLineRunner`) interroge `getUpdates` toutes les 3 s (long polling avec `offset`), analyse chaque message texte et répond via `sendMessage` (Markdown, en réponse au message d'origine) :
- `meteo <ville>` / `meteo forecast <ville>` : météo actuelle ou prévisions à midi (ville par défaut : Le Mans)
- `blague` (filtres `bonne` / `nulle`) : blague aléatoire filtrée par note
- `addjoke titre|texte|note`, `getjoke <id|mot>`, `updatejoke id|titre|texte|note`, `deletejoke <id>` (avec ou sans `/`)
- tout autre message : transmis à Mistral AI (`open-mistral-7b`, API Chat Completions)

## API REST exposée (port 9090)
| Méthode | Route | Description |
|---|---|---|
| GET | `/weather?city=Paris&forecast=true` | Météo + prévisions des 2 prochains jours |
| GET | `/joke` | Blague aléatoire (JSON) |
| POST | `/message?text=...&chatId=...` | Envoie un message Telegram (chat par défaut si `chatId` absent) |

## Configuration et lancement
Renseigner dans `src/main/resources/application.properties` : `telegram.bot.id` (token du bot), `telegram.chat.id`, `open.weather.api.token`, `ai.api.token`. Puis :
```bash
mvn spring-boot:run
```
Limites : blagues stockées en mémoire (perdues au redémarrage), pas de tests automatisés.
