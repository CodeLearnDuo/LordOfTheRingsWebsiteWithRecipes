package blps.duo.project.services;

import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.repositories.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RaceService raceService;
    private final IngredientService ingredientService;

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




}
