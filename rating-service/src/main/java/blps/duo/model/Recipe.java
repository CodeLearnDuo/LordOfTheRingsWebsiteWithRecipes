package blps.duo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("recipe")
public class Recipe {
    @Id
    private Long id;
    private String title;
    @Column("race_id")
    private Long raceId;
    private double rating;

    public Recipe(Long id, String title, Long raceId, double rating) {
        this.id = id;
        this.title = title;
        this.raceId = raceId;
        this.rating = rating;
    }

}
