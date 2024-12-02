package blps.duo.repository;

import blps.duo.model.Leader;
import blps.duo.model.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LeaderRepository extends ReactiveCrudRepository<Leader, Long> {

    Mono<Leader> findByEmail(String email);

}
