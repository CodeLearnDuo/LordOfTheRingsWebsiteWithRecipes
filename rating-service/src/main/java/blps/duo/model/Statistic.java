package blps.duo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Data
@Table("statistic")
public class Statistic {

    @Id
    private Long id;
    @Column("person_email")
    private String personEmail;
    @Column("recipe_id")
    private Long recipeId;
    @Column("value")
    private double value;
    @Column("at")
    private Timestamp at;


    public Statistic(String personEmail, Long recipeId, double value, Timestamp at) {
        this.personEmail = personEmail;
        this.recipeId = recipeId;
        this.value = value;
        this.at = at;
    }
}
