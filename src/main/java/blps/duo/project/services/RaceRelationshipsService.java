package blps.duo.project.services;

import blps.duo.project.exceptions.RaceRelationshipNotFoundException;
import blps.duo.project.model.Race;
import blps.duo.project.model.RaceRelationship;
import blps.duo.project.repositories.RaceRelationshipsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RaceRelationshipsService {

    private final RaceRelationshipsRepository raceRelationshipsRepository;
    private final RaceService raceService;

    public Mono<RaceRelationship> findRelationshipByIds(Long personId, Long recipeId) {
        return Mono.zip(
                raceService.getRaceById(personId)
                        .map(Race::getId),
                raceService.getRaceById(recipeId)
                        .map(Race::getId)
        ).flatMap(tuple -> raceRelationshipsRepository
                .findRaceRelationshipByPersonRaceIdAndRecipeRaceId(tuple.getT1(), tuple.getT2())
                .switchIfEmpty(Mono.error(new RaceRelationshipNotFoundException())));
    }
}
