package blps.duo.project.services;

import blps.duo.project.model.Statistic;
import blps.duo.project.repositories.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;
    private final TransactionalOperator requiredTransactionalOperator;

    @Transactional
    public Mono<Void> collectData(Long personId, Long recipeId, boolean value, Timestamp at) {
        return requiredTransactionalOperator.transactional(
                statisticRepository.findStatisticByPersonIdAndRecipeId(personId, recipeId)
                        .flatMap(statistic -> {
                            statistic.setValue(value);
                            statistic.setAt(at);
                            return statisticRepository.save(statistic);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            Statistic newStatistic = new Statistic(personId, recipeId, value, at);
                            return statisticRepository.save(newStatistic);
                        })).then()
        );

    }

    public Mono<Statistic> findEstimate(Long personId, Long recipeId) {
        return statisticRepository.findStatisticByPersonIdAndRecipeId(personId, recipeId);
    }

    public Mono<Void> removeEstimate(Long personId, Long recipeId) {
        return requiredTransactionalOperator.transactional(
                statisticRepository.removeStatisticByPersonIdAndRecipeId(personId, recipeId)
        );
    }


}
