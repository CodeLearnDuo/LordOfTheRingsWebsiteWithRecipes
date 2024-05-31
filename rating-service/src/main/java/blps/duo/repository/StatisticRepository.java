package blps.duo.repository;

import blps.duo.model.Statistic;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.sql.Timestamp;

@Repository
public interface StatisticRepository extends ReactiveCrudRepository<Statistic, Long> {

    @Query("SELECT * FROM statistic JOIN public.recipe r on r.id = statistic.recipe_id WHERE at >= :timestamp and r.id = :raceId")
    Flux<Statistic> findAllByRaceIdSAndSince(Long raceId, Timestamp timestamp);
}
