package blps.duo.project.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RaceRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    private Race personRace;
    @ManyToOne
    private Race recipeRace;
    private double relationshipCoefficient;
}
