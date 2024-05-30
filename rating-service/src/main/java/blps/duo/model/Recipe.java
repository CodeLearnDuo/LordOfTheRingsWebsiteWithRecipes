package blps.duo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("recipe")
public class Recipe {
    @Id
    @Column("statistic_recipe_id")
    private Long statistic_recipe_id;
    @Column("recipe_id")
    private Long recipeId;
    private String title;
    @Column("race_id")
    private Long raceId;
    private double rating;

    public Recipe(Long recipeId, String title, Long raceId, double rating) {
        this.recipeId = recipeId;
        this.title = title;
        this.raceId = raceId;
        this.rating = rating;
    }

}
