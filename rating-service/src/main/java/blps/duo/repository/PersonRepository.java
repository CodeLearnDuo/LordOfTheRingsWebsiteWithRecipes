package blps.duo.repository;

import blps.duo.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, String> {
    Mono<Person> findByEmail(String email);
}
