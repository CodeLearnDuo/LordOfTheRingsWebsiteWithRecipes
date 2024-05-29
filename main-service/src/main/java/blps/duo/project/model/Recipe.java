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
    @Column("logo_url")
    private String logoUrl;
    @Column("race_id")
    private Long raceId;
    private double rank;

    public Recipe(String title, String description, String logoUrl, Long raceId) {
        this.title = title;
        this.description = description;
        this.logoUrl = logoUrl;
        this.raceId = raceId;
    }
}
