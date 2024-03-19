package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.DeletePersonRequest;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.model.Ingredient;
import blps.duo.project.model.Recipe;
import blps.duo.project.repositories.IngredientRepository;
import blps.duo.project.repositories.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RaceService raceService;
    private final IngredientService ingredientService;
    private final ApiTokenService apiTokenService;
    private final IngredientRepository ingredientRepository;

    public Mono<RecipeResponse> getRecipeResponseById(Long recipeId) {

        return recipeRepository
                .findById(recipeId)
                .flatMap(recipe ->
                        raceService.getRaceResponseById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        ingredientService.getAllRecipesIngredients(recipeId)
                                                .collectList()
                                                .map(ingredients ->
                                                        new RecipeResponse(
                                                                recipe.getId(),
                                                                recipe.getTitle(),
                                                                recipe.getDescription(),
                                                                recipe.getLogo(),
                                                                raceResponse,
                                                                ingredients,
                                                                recipe.getRank()
                                                        ))
                                ));
    }

    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeRepository.findAll()
                .flatMap(recipe ->
                        raceService.getRaceResponseById(recipe.getRaceId())
                                                .map(raceResponse ->
                                                        new ShortRecipeResponse(
                                                                recipe.getId(),
                                                                recipe.getTitle(),
                                                                recipe.getLogo(),
                                                                raceResponse,
                                                                recipe.getRank()
                                                        ))
                                );
    }

//    @Transactional
//    public Mono<RecipeResponse> addRecipe(Long personId, ApiToken apiToken, AddRecipeRequest addRecipeRequest) {
//        return apiTokenService.getApiTokenOwner(apiToken)
//                .switchIfEmpty(Mono.error(new AuthorizationException()))
//                .flatMap(person ->
//                       (raceService.getRaceResponseById(personId)
//                               .map(RaceResponse::name))
//                               .flatMap(raceService::getRaceByRaceName)
//                                .flatMap(raceResponse ->
//                                        recipeRepository.save(new Recipe(
//                                                addRecipeRequest.title(),
//                                                addRecipeRequest.description(),
//                                                addRecipeRequest.logo(),
//                                                raceResponse.getId()
//                                                )
//                                        )
//                                )
//                               .flatMap(recipe -> ingredientService.saveAllIngredientsForRecipeId(recipe.getId(), addRecipeRequest.ingredients())
//
//                               )))
//                )
//    }

}
