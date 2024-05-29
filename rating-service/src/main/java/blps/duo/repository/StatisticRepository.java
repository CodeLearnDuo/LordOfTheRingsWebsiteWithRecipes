package blps.duo.repository;

import blps.duo.model.Statistic;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticRepository extends ReactiveCrudRepository<Statistic, Long> {
}
