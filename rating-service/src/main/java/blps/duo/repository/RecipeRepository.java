package blps.duo.repository;

import blps.duo.model.Recipe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
}