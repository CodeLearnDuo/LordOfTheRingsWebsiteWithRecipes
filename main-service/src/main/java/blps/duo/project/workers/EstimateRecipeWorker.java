package blps.duo.project.workers;

import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.CamundaUserProfileResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.model.Person;
import blps.duo.project.services.AssigneeService;
import blps.duo.project.services.PersonService;
import blps.duo.project.services.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class EstimateRecipeWorker {

    private final ExternalTaskClient client;
    private final RecipeService recipeService;
    private final PersonService personService;
    private final AssigneeService assigneeService;


    public EstimateRecipeWorker(RecipeService recipeService, ExternalTaskClient client, PersonService personService, AssigneeService assigneeService) {
        this.recipeService = recipeService;
        this.personService = personService;
        this.client = client;
        this.assigneeService = assigneeService;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("estimate-recipe")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        Integer recipeId = externalTask.getVariable("recipe_id_field");

        try {


            Boolean value = Boolean.valueOf(externalTask.getVariable("like_field"));
            ScoreRequest scoreRequest = new ScoreRequest(value, Integer.toUnsignedLong(recipeId));
            Mono<Person> personMono = getPersonFromContext(externalTask);
            recipeService.estimate(personMono, scoreRequest)
                    .flatMap(recipeResponse -> {
                        return recipeService.getRecipeResponseById(recipeResponse.id())
                                .flatMap(updatedRecipe -> {
                                    Map<String, Object> variables = mapToProcessVariables(updatedRecipe);
                                    externalTaskService.complete(externalTask, variables);
                                    return Mono.just(updatedRecipe);
                                });
                    })
                    .doOnError(error -> {
                        log.error("Ошибка в estimate: {}", error.getMessage());
                        handleError(error, externalTask, externalTaskService);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Ошибка в estimate: {}", e.getMessage(), e);
            handleError(e, externalTask, externalTaskService);
        }
    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("errorCode", "BP_ERROR");
        variables.put("errorMessage", throwable.getMessage());
        externalTaskService.handleBpmnError(externalTask, "BP_ERROR", throwable.getMessage(), variables);
        log.error("Form data error: {}", throwable.getMessage(), throwable);
    }

    private Mono<Person> getPersonFromContext(ExternalTask externalTask) {
        String userId = externalTask.getVariable("initiator");

        return assigneeService
                .getUserByUserId(userId)
                .map(CamundaUserProfileResponse::email)
                .flatMap(personService::getPersonByEmail);
    }

    private Map<String, Object> mapToProcessVariables(RecipeResponse recipeResponse) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipeId", recipeResponse.id());
        variables.put("recipeTitle", recipeResponse.title());
        variables.put("recipeRank", recipeResponse.rank());
        return variables;
    }
}