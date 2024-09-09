package blps.duo.project.workers;

import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.services.RecipeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FindRecipeBeforeEstimate {

    private final ExternalTaskClient client;
    private final RecipeService recipeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FindRecipeBeforeEstimate(RecipeService recipeService, ExternalTaskClient client) {
        this.recipeService = recipeService;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("find-recipe-before-estimate")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String recipeName = externalTask.getVariable("recipe_name_field");

        recipeService.findRecipeByName(recipeName)
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found")))
                .collectList()
                .map(this::mapToProcessVariables)
                .flatMap(variables -> {
                    externalTaskService.complete(externalTask, variables);
                    return Mono.empty();
                })
                .onErrorResume(error -> {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("errorMessage", error.getMessage());
                    externalTaskService.handleFailure(externalTask, error.getMessage(), error.getMessage(), 0, 0);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Map<String, Object> mapToProcessVariables(List<ShortRecipeResponse> recipes) {
        Map<String, Object> variables = new HashMap<>();

        try {
            String recipesJson = objectMapper.writeValueAsString(recipes);
            variables.put("foundRecipes", recipesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing recipes to JSON", e);
        }

        return variables;
    }
}

