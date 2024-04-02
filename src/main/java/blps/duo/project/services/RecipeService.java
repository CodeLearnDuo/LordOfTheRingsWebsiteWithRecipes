package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.exceptions.NoSuchRecipeException;
import blps.duo.project.model.Race;
import blps.duo.project.model.RaceRelationship;
import blps.duo.project.model.Recipe;
import blps.duo.project.model.Statistic;
import blps.duo.project.repositories.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.sql.Timestamp;
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

    public Mono<RecipeResponse> getRecipeResponseById(Long recipeId) {

        return recipeRepository
                .findById(recipeId)
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        ingredientService.getAllRecipesIngredients(recipeId)
                                                .collectList()
                                                .map(ingredients ->
                                                        new RecipeResponse(
                                                                recipe.getId(),
                                                                recipe.getTitle(),
                                                                recipe.getDescription(),
                                                                recipe.getLogo(),
                                                                new RaceResponse(raceResponse.getName()),
                                                                ingredients,
                                                                recipe.getRank()
                                                        ))
                                ));
    }

    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeRepository.findAll()
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .map(raceResponse ->
                                        new ShortRecipeResponse(
                                                recipe.getId(),
                                                recipe.getTitle(),
                                                recipe.getLogo(),
                                                new RaceResponse(raceResponse.getName()),
                                                recipe.getRank()
                                        ))
                );
    }

    @Transactional
    public Mono<RecipeResponse> addRecipe(ApiToken apiToken, AddRecipeRequest addRecipeRequest) {
        return apiTokenService.getApiTokenOwner(apiToken)
                .switchIfEmpty(Mono.error(new AuthorizationException()))
                .flatMap(person ->
                        (raceService.getRaceById(person.getPersonRaceId())
                                .map(Race::getName))
                                .flatMap(raceService::getRaceByRaceName)
                                .flatMap(race ->
                                        recipeRepository.save(new Recipe(
                                                        addRecipeRequest.title(),
                                                        addRecipeRequest.description(),
                                                        addRecipeRequest.logo(),
                                                        race.getId()
                                                )
                                        ).flatMap(recipe -> ingredientService.saveAllIngredientsForRecipeId(recipe.getId(), addRecipeRequest.ingredients())
                                                .collectList()
                                                .flatMap(ingredientsResponseList ->
                                                        Mono.just(new RecipeResponse(
                                                                recipe.getId(),
                                                                recipe.getTitle(),
                                                                recipe.getDescription(),
                                                                recipe.getLogo(),
                                                                new RaceResponse(race.getName()),
                                                                ingredientsResponseList,
                                                                recipe.getRank()
                                                        ))

                                                )))
                );
    }

    public Flux<ShortRecipeResponse> findRecipeByName(String recipeName) {

        final Pattern pattern = Pattern.compile(recipeName);

        return recipeRepository.findAll()
                .filter(recipe -> pattern.matcher(recipe.getTitle()).find())
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .map(race -> Tuples.of(recipe, race))
                )
                .map(tuple -> {
                    var recipe = tuple.getT1();
                    var race = tuple.getT2();
                    return new ShortRecipeResponse(
                            recipe.getId(),
                            recipe.getTitle(),
                            recipe.getLogo(),
                            new RaceResponse(race.getName()),
                            recipe.getRank()
                    );
                });
    }

    @Transactional
    public Mono<RecipeResponse> estimate(ApiToken apiToken, ScoreRequest scoreRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return apiTokenService.getApiTokenOwner(apiToken)
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
                .flatMap(recipe -> getRecipeResponseById(recipe.getId()));
    }


    private Mono<Recipe> updateRankByRemovingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double estimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() - estimation);
                    return recipeRepository.save(recipe);
                });
    }

    private Mono<Recipe> updateRankByChangingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double currentEstimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());
        double previousEstimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() - previousEstimation + currentEstimation);
                    return recipeRepository.save(recipe);
                });
    }

    private Mono<Recipe> updateRankByAddingEstimate(ApiToken apiToken, ScoreRequest scoreRequest, RaceRelationship raceRelationship) {

        double estimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return recipeRepository.findById(scoreRequest.recipeId())
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe -> {
                    recipe.setRank(recipe.getRank() + estimation);
                    return recipeRepository.save(recipe);
                });
    }


}
