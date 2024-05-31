package blps.duo.project.repositories;

import blps.duo.project.model.Person;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Person> findByEmail(String email);

    @Query("SELECT * FROM person WHERE a_leader = true")
    Flux<Person> findAllLeaders();

}
