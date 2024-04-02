package blps.duo.project.repositories;

import blps.duo.project.model.Statistic;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StatisticRepository extends ReactiveCrudRepository<Statistic, Long> {

    Mono<Statistic> findStatisticByPersonIdAndRecipeId(Long personId, Long recipeId);

    Mono<Void> removeStatisticByPersonIdAndRecipeId(Long personId, Long recipeId);
}
