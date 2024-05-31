package blps.duo.repository;

import blps.duo.model.Leader;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderRepository extends ReactiveCrudRepository<Leader, String> {
}
