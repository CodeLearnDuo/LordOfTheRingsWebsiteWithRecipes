package blps.duo.project.controllers;

import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.model.Person;
import blps.duo.project.services.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    //all
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<RecipeResponse> getRecipe(@PathVariable Long id) {
        return recipeService.getRecipeResponseById(id);
    }

    //all
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeService.getAllShortRecipes();
    }

    //authorised
    @PostMapping
    public Mono<AddRecipeResponse> addRecipe(@AuthenticationPrincipal Mono<Person> requestOwnerMono,
                                          @RequestBody @Valid AddRecipeRequest addRecipeRequest) {
        return recipeService.addRecipe(requestOwnerMono, addRecipeRequest);
    }

    //authorised
    @PostMapping("/estimation")
    public Mono<RecipeResponse> estimate(@AuthenticationPrincipal Mono<Person> requestOwnerMono,
                                         @RequestBody @Valid ScoreRequest scoreRequest) {
        return recipeService.estimate(requestOwnerMono, scoreRequest);
    }
}
