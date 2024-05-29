package blps.duo.project.model;

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
    @Column("person_id")
    private Long personId;
    @Column("recipe_id")
    private Long recipeId;
    @Column("value")
    private boolean value;
    @Column("at")
    private Timestamp at;


    public Statistic(Long personId, Long recipeId, boolean value, Timestamp at) {
        this.personId = personId;
        this.recipeId = recipeId;
        this.value = value;
        this.at = at;
    }
}
