package blps.duo.project.services;

import blps.duo.project.dto.responses.IngredientsResponse;
import blps.duo.project.repositories.IngredientRepository;
import blps.duo.project.repositories.RecipeIngredientRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRelationRepository recipeIngredientRelationRepository;

    public Flux<IngredientsResponse> getAllRecipesIngredients(Long recipeId) {

        return recipeIngredientRelationRepository.findAllByRecipeId(recipeId)
                .flatMap(relation ->
                        ingredientRepository.findById(relation.getIngredientId())
                                .map(ingredient -> new IngredientsResponse(
                                        ingredient.getId(),
                                        ingredient.getName(),
                                        ingredient.getDescription()
                                )));
    }

}
