package blps.duo.project.repositories;

import blps.duo.project.model.Recipe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
}
