package blps.duo.project.repositories;

import blps.duo.project.model.RecipeIngredientRelation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecipeIngredientRelationRepository extends ReactiveCrudRepository<RecipeIngredientRelation, Long> {

    Flux<RecipeIngredientRelation> findAllByRecipeId(Long recipeId);

}
