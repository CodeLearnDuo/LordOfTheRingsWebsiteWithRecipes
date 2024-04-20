package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.AddRecipeResponse;
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
        return recipeService.getRecipeResponseById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeService.getAllShortRecipes();
    }

    @PostMapping
    public Mono<AddRecipeResponse> addRecipe(@RequestHeader("ApiToken") @Valid ApiToken apiToken, @RequestBody @Valid AddRecipeRequest addRecipeRequest) {
        return recipeService.addRecipe(apiToken, addRecipeRequest);
    }

    @PostMapping("/estimation")
    public Mono<RecipeResponse> estimate(@RequestHeader("ApiToken") @Valid ApiToken apiToken, @RequestBody @Valid ScoreRequest scoreRequest) {
        return recipeService.estimate(apiToken, scoreRequest);
    }


}
