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

        String personEmail = assigneeService
                .getAssigneeUser(externalTask.getProcessInstanceId())
                .map(CamundaUserProfileResponse::email)
                .block();
        Integer recipeId = externalTask.getVariable("recipe_id_field");

        Boolean value = Boolean.valueOf(externalTask.getVariable("like_field"));
        ScoreRequest scoreRequest = new ScoreRequest(value, Integer.toUnsignedLong(recipeId));

        Mono<Person> personMono = personService.getPersonByEmail(personEmail);


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
                    externalTaskService.handleFailure(externalTask, error.getMessage(), error.getMessage(), 0, 0);
                })
                .subscribe();

    }


    private Map<String, Object> mapToProcessVariables(RecipeResponse recipeResponse) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipeId", recipeResponse.id());
        variables.put("recipeTitle", recipeResponse.title());
        variables.put("recipeRank", recipeResponse.rank());
        return variables;
    }
}

