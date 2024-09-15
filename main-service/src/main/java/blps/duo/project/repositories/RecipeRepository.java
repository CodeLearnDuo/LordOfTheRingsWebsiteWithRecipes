package blps.duo.project.repositories;

import blps.duo.project.model.Recipe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {

    Mono<Recipe> findByTitle(String name);
}
