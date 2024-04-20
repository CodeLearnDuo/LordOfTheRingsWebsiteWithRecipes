package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.exceptions.AuthorizationException;
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
    private final ApiTokenService apiTokenService;
    private final RaceRelationshipsService raceRelationshipsService;
    private final StatisticService statisticService;
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


    public Mono<AddRecipeResponse> addRecipe(ApiToken apiToken, AddRecipeRequest addRecipeRequest) {
        return requiresNewReadCommitedTransactionalOperator.transactional(
                apiTokenService.getApiTokenOwner(apiToken)
                        .switchIfEmpty(Mono.error(new AuthorizationException()))
                        .flatMap(person ->
                                (raceService.getRaceById(person.getPersonRaceId())
                                        .map(Race::getName))
                                        .flatMap(raceService::getRaceByRaceName)
                                        .flatMap(race ->
                                                {
                                                    String logoId = UUID.randomUUID().toString();
                                                    redisRepository.addLogo(new Logo(logoId, addRecipeRequest.logo()));
                                                    return recipeRepository.save(new Recipe(
                                                                    addRecipeRequest.title(),
                                                                    addRecipeRequest.description(),
                                                                    logoId,
                                                                    race.getId()
                                                            ))
                                                            .flatMap(recipe -> ingredientService.saveAllIngredientsForRecipeId(recipe.getId(), addRecipeRequest.ingredients())
                                                                    .collectList()
                                                                    .flatMap(ingredientsResponseList ->
                                                                            Mono.just(new AddRecipeResponse(
                                                                                    recipe.getId(),
                                                                                    recipe.getTitle(),
                                                                                    recipe.getDescription(),
                                                                                    logoId,
                                                                                    new RaceResponse(race.getName()),
                                                                                    ingredientsResponseList,
                                                                                    recipe.getRank()
                                                                            ))

                                                                    ));
                                                }
                                        )
                        )
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

    public Mono<RecipeResponse> estimate(ApiToken apiToken, ScoreRequest scoreRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return requiredNewTransactionalOperator.transactional(
                apiTokenService.getApiTokenOwner(apiToken)
                        .switchIfEmpty(Mono.error(new AuthorizationException()))
                        .flatMap(person -> raceRelationshipsService.findRelationshipByIds(apiToken.apiToken(), scoreRequest.recipeId()))
                        .flatMap(raceRelationship ->
                                statisticService.findEstimate(apiToken.apiToken(), scoreRequest.recipeId())
                                        .flatMap(foundEstimation -> {
                                            if (foundEstimation.isValue() == scoreRequest.value()) {
                                                return updateRankByRemovingEstimate(apiToken, scoreRequest, raceRelationship, foundEstimation)
                                                        .flatMap(recipe -> statisticService.removeEstimate(apiToken.apiToken(), scoreRequest.recipeId())
                                                                .thenReturn(recipe));
                                            } else {
                                                return updateRankByChangingEstimate(apiToken, scoreRequest, raceRelationship, foundEstimation)
                                                        .flatMap(recipe -> statisticService.collectData(apiToken.apiToken(), scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                                                                .thenReturn(recipe));
                                            }
                                        })
                                        .switchIfEmpty(
                                                updateRankByAddingEstimate(apiToken, scoreRequest, raceRelationship)
                                                        .flatMap(recipe -> statisticService.collectData(apiToken.apiToken(), scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                                                                .thenReturn(recipe))
                                        )
                        )
                        .flatMap(recipe -> getRecipeResponseById(recipe.getId()))
        );
    }


    private Mono<Recipe> updateRankByRemovingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double estimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() - estimation);
                            return recipeRepository.save(recipe);
                        })
        );
    }

    private Mono<Recipe> updateRankByChangingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double currentEstimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());
        double previousEstimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() - previousEstimation + currentEstimation);
                            return recipeRepository.save(recipe);
                        })
        );
    }

    private Mono<Recipe> updateRankByAddingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship) {

        double estimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() + estimation);
                            return recipeRepository.save(recipe);
                        })
        );
    }


}
