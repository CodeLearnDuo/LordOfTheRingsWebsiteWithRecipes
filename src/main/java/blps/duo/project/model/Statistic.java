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
    private Timestamp at;


}
