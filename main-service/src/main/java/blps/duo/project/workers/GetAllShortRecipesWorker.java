package blps.duo.project.workers;

import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.services.RecipeService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetAllShortRecipesWorker {

    private final RecipeService recipeService;

    public GetAllShortRecipesWorker(RecipeService recipeService, ExternalTaskClient client) {
        this.recipeService = recipeService;
        subscribeToTask(client);
    }

    private void subscribeToTask(ExternalTaskClient client) {
        client.subscribe("get-all-short-recipes")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        recipeService.getAllShortRecipes()
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
        variables.put("foundRecipes", recipes);
        return variables;
    }
}

