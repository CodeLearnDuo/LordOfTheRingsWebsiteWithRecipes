package blps.duo.project.services;

import blps.duo.project.model.Statistic;
import blps.duo.project.repositories.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

    public Mono<Void> collectData(Long personId, Long recipeId, boolean value, Timestamp at) {
        statisticRepository.save(new Statistic(
                personId,
                recipeId,
                value,
                at
        ));
        return Mono.empty();
    }
}
