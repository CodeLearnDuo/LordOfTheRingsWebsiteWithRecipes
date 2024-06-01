package blps.duo.repository;

import blps.duo.model.ExpandedStatistic;
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


    @Query("SELECT s.id AS id, s.value AS value, s.at AS at, s.person_email AS personEmail, p.person_race_id AS personRaceId, p.rating_count AS personRatingCount, s.recipe_id AS recipeId, r.title AS recipeTitle, r.race_id AS recipeRaceId, r.rating AS recipeRating " +
            "FROM statistic s " +
            "JOIN public.recipe r ON r.id = s.recipe_id " +
            "JOIN public.person p ON p.email = s.person_email " +
            "WHERE at >= :timestamp and r.id = :raceId")
    Flux<ExpandedStatistic> findExpandedStatisticByRaceIdSAndSince(Long raceId, Timestamp timestamp);
}
