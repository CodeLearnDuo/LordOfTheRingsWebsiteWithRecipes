package blps.duo.project.repositories;

import blps.duo.project.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {
}
