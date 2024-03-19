package blps.duo.project.repositories;

import blps.duo.project.model.Ingredient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, Long> {

    Mono<Boolean> existsByNameAndDescription(String name, String description);

    Mono<Ingredient> findByNameAndDescription(String name, String description);
}
