package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.services.RecipeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Data
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<RecipeResponse> getRecipe(@PathVariable Long id) {
        //TODO validation
        return recipeService.getRecipeResponseById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        //TODO validation
        return recipeService.getAllShortRecipes();
    }

    @PostMapping
    public Mono<RecipeResponse> addRecipe(@RequestHeader("ApiToken") @Valid ApiToken apiToken, @RequestBody @Valid AddRecipeRequest addRecipeRequest) {
        //TODO validation
        return recipeService.addRecipe(apiToken, addRecipeRequest);
    }


}
