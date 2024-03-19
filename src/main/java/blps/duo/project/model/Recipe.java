package blps.duo.project.model;

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
    private String description;
    private byte[] logo;
    @Column("race_id")
    private Long raceId;
    private double rank;

    public Recipe(String title, String description, byte[] logo, Long raceId) {
        this.title = title;
        this.description = description;
        this.logo = logo;
        this.raceId = raceId;
    }
}
