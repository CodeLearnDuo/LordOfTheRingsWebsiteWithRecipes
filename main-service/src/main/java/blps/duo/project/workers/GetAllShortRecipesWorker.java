package blps.duo.project.workers;

import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.services.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
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
                    handleError(error, externalTask, externalTaskService);
                    return Mono.empty();
                })
                .subscribe();
    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("errorCode", "BP_ERROR");
        variables.put("errorMessage", throwable.getMessage());
        externalTaskService.handleBpmnError(externalTask, "BP_ERROR", throwable.getMessage(), variables);
        log.error("Form data error: {}", throwable.getMessage(), throwable);
    }

    private Map<String, Object> mapToProcessVariables(List<ShortRecipeResponse> recipes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("foundRecipes", recipes);
        return variables;
    }
}

