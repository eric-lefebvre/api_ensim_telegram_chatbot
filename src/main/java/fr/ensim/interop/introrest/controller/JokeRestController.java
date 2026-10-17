package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.joke.Joke;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
public class JokeRestController {

    private static final List<Joke> JOKES = Arrays.asList(
        new Joke(1, "Le programmeur optimiste", "Un programmeur dit à sa femme : 'Va faire les courses. Prends un litre de lait, et si ils ont des oeufs, prends-en 6.' Elle revient avec 6 litres de lait. 'Ils avaient des oeufs.'", 8.5),
        new Joke(2, "L'ascenseur", "Pourquoi les plongeurs plongent-ils toujours en arrière et jamais en avant ? Parce que sinon ils tomberaient dans le bateau.", 7.0),
        new Joke(3, "Le facteur", "Qu'est-ce qu'un crocodile qui surveille des valises ? Un sac à dents.", 6.5),
        new Joke(4, "La bibliothèque", "Un homme entre dans une bibliothèque et demande : 'Vous avez des livres sur la paranoïa ?' La bibliothécaire chuchote : 'Ils sont juste derrière vous !'", 9.0),
        new Joke(5, "Le menuisier", "Qu'est-ce qu'un canif ? Un petit fien.", 5.0)
    );

    private final Random random = new Random();

    @GetMapping("/joke")
    public Joke getRandomJoke() {
        return JOKES.get(random.nextInt(JOKES.size()));
    }
}
