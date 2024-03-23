package blps.duo.project.services;

import blps.duo.project.exceptions.NoSuchRecipeException;
import blps.duo.project.exceptions.RaceRelationshipNotFoundException;
import blps.duo.project.model.RaceRelationship;
import blps.duo.project.repositories.RaceRelationshipsRepository;
import blps.duo.project.repositories.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RaceRelationshipsService {

    private final RaceRelationshipsRepository raceRelationshipsRepository;
    private final PersonService personService;
    private final RecipeRepository recipeRepository;

    public Mono<RaceRelationship> findRelationshipByIds(Long personId, Long recipeId) {
        return Mono.zip(
                        personService.getPersonById(personId),
                        recipeRepository.findById(recipeId).switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                )
                .flatMap(tuple -> {
                    Long personRaceId = tuple.getT1().getPersonRaceId();
                    Long recipeRaceId = tuple.getT2().getRaceId();
                    return raceRelationshipsRepository.findRaceRelationshipByPersonRaceIdAndRecipeRaceId(personRaceId, recipeRaceId)
                            .switchIfEmpty(Mono.error(new RaceRelationshipNotFoundException()));
                });
    }
}
