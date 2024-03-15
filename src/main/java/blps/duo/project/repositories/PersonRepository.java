package blps.duo.project.repositories;

import blps.duo.project.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {

    Mono<Boolean> existsByEmail(String email);
}
