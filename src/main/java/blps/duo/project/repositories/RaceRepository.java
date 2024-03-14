package blps.duo.project.repositories;

import blps.duo.project.model.Race;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceRepository extends ReactiveCrudRepository<Race, Long> {
}
