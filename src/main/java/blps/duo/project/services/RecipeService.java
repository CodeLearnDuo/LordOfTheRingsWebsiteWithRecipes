package blps.duo.project.services;

import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.exceptions.JwtAuthenticationException;
import blps.duo.project.exceptions.NoSuchRecipeException;
import blps.duo.project.model.*;
import blps.duo.project.repositories.RecipeRepository;
import blps.duo.project.repositories.redis.RedisRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RaceService raceService;
    private final IngredientService ingredientService;
    private final RaceRelationshipsService raceRelationshipsService;
    private final StatisticService statisticService;
    private final PersonService personService;
    private final TransactionalOperator requiredTransactionalOperator;
    private final TransactionalOperator requiredNewTransactionalOperator;
    private final TransactionalOperator requiresNewReadCommitedTransactionalOperator;
    private final RedisRepositoryImpl redisRepository;

    public Mono<RecipeResponse> getRecipeResponseById(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        ingredientService.getAllRecipesIngredients(recipeId)
                                                .collectList()
                                                .flatMap(ingredients ->
                                                        redisRepository.findLogo(recipe.getLogo())
                                                                .defaultIfEmpty(new Logo("", new byte[0]))
                                                                .map(logo ->
                                                                        new RecipeResponse(
                                                                                recipe.getId(),
                                                                                recipe.getTitle(),
                                                                                recipe.getDescription(),
                                                                                logo.getImage(),
                                                                                new RaceResponse(raceResponse.getName()),
                                                                                ingredients,
                                                                                recipe.getRank()
                                                                        )
                                                                )
                                                ))
                );
    }

    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeRepository.findAll()
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        redisRepository.findLogo(recipe.getLogo())
                                                .defaultIfEmpty(new Logo("", new byte[0]))
                                                .map(logo -> new ShortRecipeResponse(
                                                        recipe.getId(),
                                                        recipe.getTitle(),
                                                        logo.getImage(),
                                                        new RaceResponse(raceResponse.getName()),
                                                        recipe.getRank()
                                                )))
                );
    }

    public Mono<AddRecipeResponse> addRecipe(Mono<Person> requestOwnerMono, AddRecipeRequest addRecipeRequest) {
        return requiresNewReadCommitedTransactionalOperator.transactional(
                requestOwnerMono
                        .switchIfEmpty(Mono.error(new AuthorizationException()))
                        .flatMap(requestOwner -> raceService.existsRaceById(requestOwner.getPersonRaceId())
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid race in jwt")))
                                .map(exists -> requestOwner))
                        .flatMap(requestOwner -> {
                            String logoId = UUID.randomUUID().toString();
                            redisRepository.addLogo(new Logo(logoId, addRecipeRequest.logo()));

                            return recipeRepository.save(new Recipe(
                                            addRecipeRequest.title(),
                                            addRecipeRequest.description(),
                                            logoId,
                                            requestOwner.getPersonRaceId()
                                    )
                            ).flatMap(recipe -> ingredientService
                                    .saveAllIngredientsForRecipeId(recipe.getId(), addRecipeRequest.ingredients())
                                    .collectList()
                                    .map(ingredientsResponseList ->
                                            new AddRecipeResponse(
                                                    recipe.getId(),
                                                    recipe.getTitle(),
                                                    recipe.getDescription(),
                                                    logoId,
                                                    new RaceResponse(requestOwner.getName()),
                                                    ingredientsResponseList,
                                                    recipe.getRank()
                                            )
                                    )
                            );
                        })
        );
    }


    public Flux<ShortRecipeResponse> findRecipeByName(String recipeName) {
        final Pattern pattern = Pattern.compile(recipeName);

        return recipeRepository.findAll()
                .filter(recipe -> pattern.matcher(recipe.getTitle()).find())
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(race ->
                                        redisRepository.findLogo(recipe.getLogo())
                                                .defaultIfEmpty(new Logo("", new byte[0]))
                                                .map(logo -> new ShortRecipeResponse(
                                                        recipe.getId(),
                                                        recipe.getTitle(),
                                                        logo.getImage(),
                                                        new RaceResponse(race.getName()),
                                                        recipe.getRank()
                                                )))
                );
    }

    public Mono<RecipeResponse> estimate(Mono<Person> requestOwnerMono, ScoreRequest scoreRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        record OwnerWithRaceRelationship(Person owner, RaceRelationship relationship) {
        }

        return requiredNewTransactionalOperator.transactional(
                requestOwnerMono
                .switchIfEmpty(Mono.error(new AuthorizationException()))
                .flatMap(requestOwner -> personService.getPersonByEmail(requestOwner.getEmail()))
                .flatMap(requestOwner ->
                        raceRelationshipsService.findRelationshipByIds(requestOwner.getId(), scoreRequest.recipeId())
                                .map(relationship -> new OwnerWithRaceRelationship(requestOwner, relationship))
                )
                .flatMap(ownerWithRaceRelationship -> {
                            var foundEstimationMono = statisticService.findEstimate(ownerWithRaceRelationship.owner.getId(), scoreRequest.recipeId());
                            return updateRank(
                                    ownerWithRaceRelationship.owner.getId(),
                                    scoreRequest,
                                    ownerWithRaceRelationship.relationship,
                                    foundEstimationMono,
                                    timestamp
                            );
                        }
                )
                .flatMap(recipe -> getRecipeResponseById(recipe.getId()))
        );
    }

    private Mono<Recipe> updateRank(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Mono<Statistic> foundEstimationMono, Timestamp timestamp) {
        return requiredTransactionalOperator.transactional(
                foundEstimationMono
                        .flatMap(foundEstimation -> {
                            if (foundEstimation.isValue() == scoreRequest.value()) {
                                return updateRankByRemovingEstimate(ownerId, scoreRequest, raceRelationship, foundEstimation);
                            } else {
                                return updateRankByChangingEstimate(ownerId, scoreRequest, raceRelationship, foundEstimation, timestamp);
                            }
                        })
                        .switchIfEmpty(updateRankByAddingEstimate(ownerId, scoreRequest, raceRelationship, timestamp))
        );
    }

    private Mono<Recipe> updateRankByRemovingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double estimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() - estimation);
                    return recipeRepository.save(recipe);
                })
                .flatMap(recipe -> statisticService.removeEstimate(ownerId, scoreRequest.recipeId())
                        .thenReturn(recipe))
        );
    }

    private Mono<Recipe> updateRankByChangingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation, Timestamp timestamp) {

        double currentEstimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());
        double previousEstimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() - previousEstimation + currentEstimation);
                    return recipeRepository.save(recipe);
                })
                .flatMap(recipe -> statisticService.collectData(ownerId, scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                        .thenReturn(recipe))
        );
    }

    private Mono<Recipe> updateRankByAddingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Timestamp timestamp) {

        double estimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() + estimation);
                    return recipeRepository.save(recipe);
                })
                .flatMap(recipe -> statisticService.collectData(ownerId, scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                        .thenReturn(recipe))
        );
    }


}
