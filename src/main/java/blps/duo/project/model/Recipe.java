package blps.duo.project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;
    @ManyToMany
    private List<Ingredient> ingredients;
    private String description;
    @Lob
    private byte[] logo;
    @ManyToOne
    private Race race;
    private double rank;

}
