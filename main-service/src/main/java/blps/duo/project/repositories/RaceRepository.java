package blps.duo.project.repositories;

import blps.duo.project.model.Race;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RaceRepository extends ReactiveCrudRepository<Race, Long> {

    Mono<Race> findRaceByName(String name);

}
