package blps.duo.project.services;

import blps.duo.project.dto.requests.IngredientsRequest;
import blps.duo.project.dto.responses.IngredientsResponse;
import blps.duo.project.exceptions.NoSuchRecipeException;
import blps.duo.project.model.Ingredient;
import blps.duo.project.model.RecipeIngredientRelation;
import blps.duo.project.repositories.IngredientRepository;
import blps.duo.project.repositories.RecipeIngredientRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRelationRepository recipeIngredientRelationRepository;

    public Flux<IngredientsResponse> getAllRecipesIngredients(Long recipeId) {

        return recipeIngredientRelationRepository.findAllByRecipeId(recipeId)
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(relation ->
                        ingredientRepository.findById(relation.getIngredientId())
                                .map(ingredient -> new IngredientsResponse(
                                        ingredient.getName(),
                                        ingredient.getDescription()
                                )));
    }

    @Transactional
    public Flux<IngredientsResponse> saveAllIngredientsForRecipeId(Long recipeId, List<IngredientsRequest> ingredientsRequestList) {

        return Flux.fromIterable(ingredientsRequestList)
                .flatMap(ingredient -> ingredientRepository.existsByNameAndDescription(ingredient.name(), ingredient.description())
                        .flatMap(exist -> {
                            if (Boolean.TRUE.equals(exist)) {
                                return ingredientRepository.findByNameAndDescription(ingredient.name(), ingredient.description());
                            } else {
                                return ingredientRepository.save(new Ingredient(ingredient.name(), ingredient.description()));
                            }
                        }))
                .flatMap(ingredient -> recipeIngredientRelationRepository
                        .save(new RecipeIngredientRelation(recipeId, ingredient.getId()))
                        .map(recipeIngredientRelation -> new IngredientsResponse(ingredient.getName(), ingredient.getDescription())));

    }


}
