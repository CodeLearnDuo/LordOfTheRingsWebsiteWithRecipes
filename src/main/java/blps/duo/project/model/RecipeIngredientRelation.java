package blps.duo.project.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("recipe_ingredient_relation")
public class RecipeIngredientRelation {
    @Id
    private Long id;
    @Column("recipe_id")
    private Long recipeId;
    @Column("ingredient_id")
    private Long ingredientId;
}
