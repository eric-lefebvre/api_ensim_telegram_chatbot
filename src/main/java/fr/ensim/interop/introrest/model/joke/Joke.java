package fr.ensim.interop.introrest.model.joke;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Joke {
    private int id;
    private String titre;
    private String texte;
    private double note;
}
