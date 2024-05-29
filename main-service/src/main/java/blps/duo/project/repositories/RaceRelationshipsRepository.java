package blps.duo.project.repositories;

import blps.duo.project.model.RaceRelationship;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RaceRelationshipsRepository extends ReactiveCrudRepository<RaceRelationship, Long> {
    Mono<RaceRelationship> findRaceRelationshipByPersonRaceIdAndRecipeRaceId(Long personRaceId, Long recipeRaceId);

}
