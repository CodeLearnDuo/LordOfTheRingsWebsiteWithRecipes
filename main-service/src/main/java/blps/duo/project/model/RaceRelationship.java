package blps.duo.project.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("race_relationship")
public class RaceRelationship {

    @Id
    private Long id;
    @Column("person_race_id")
    private Long personRaceId;
    @Column("recipe_race_id")
    private Long recipeRaceId;
    @Column("relationship_coefficient")
    private double relationshipCoefficient;
}
