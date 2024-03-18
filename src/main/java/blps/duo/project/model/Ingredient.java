package blps.duo.project.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ingredient")
public class Ingredient {
    @Id
    private Long id;
    private String name;
    private String description;
}
