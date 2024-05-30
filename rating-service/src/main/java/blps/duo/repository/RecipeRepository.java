package blps.duo.repository;

import blps.duo.model.Recipe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
    Mono<Recipe> findByRecipeId(Long recipeId);
}