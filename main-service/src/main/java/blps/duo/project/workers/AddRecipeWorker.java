package blps.duo.project.workers;

import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.IngredientsRequest;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.model.Person;
import blps.duo.project.services.PersonService;
import blps.duo.project.services.RecipeService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AddRecipeWorker {

    private final RecipeService recipeService;
    private final PersonService personService;
    private final ExternalTaskClient client;

    public AddRecipeWorker(RecipeService recipeService, PersonService personService, ExternalTaskClient client) {
        this.recipeService = recipeService;
        this.personService = personService;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("add-recipe-task")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String title = externalTask.getVariable("title_field");
        String description = externalTask.getVariable("description_field");
        String ingredientsString = externalTask.getVariable("ingredients_field");
        Mono<FilePart> logoFileMono = getFilePartFromProcess(externalTask);

        List<IngredientsRequest> ingredients = parseIngredients(ingredientsString);

        AddRecipeRequest addRecipeRequest = new AddRecipeRequest(title, description, ingredients);

        Mono<Person> requestOwnerMono = getPersonFromContext(externalTask);

        System.out.println(addRecipeRequest);
        System.out.println(requestOwnerMono.block());
        System.out.println(logoFileMono);

        recipeService.addRecipe(requestOwnerMono, addRecipeRequest, logoFileMono)
                .doOnSuccess(addRecipeResponse -> handleSuccess(addRecipeResponse, externalTask, externalTaskService))
                .doOnError(throwable -> handleError(throwable, externalTask, externalTaskService))
                .subscribe();
    }

    private Mono<FilePart> getFilePartFromProcess(ExternalTask externalTask) {
        return Mono.empty();
    }

    private Mono<Person> getPersonFromContext(ExternalTask externalTask) {
        return personService.getPersonByEmail(externalTask.getVariable("email_field"));
    }

    private void handleSuccess(AddRecipeResponse response, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("response", response);
        variables.put("Add status", "success");
        externalTaskService.complete(externalTask, variables);
    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        externalTaskService.handleFailure(externalTask, throwable.getMessage(), "Error while adding recipe", 0, 1000);
    }

    private static List<IngredientsRequest> parseIngredients(String ingredientsString) {
        String[] ingredientsArray = ingredientsString.split(";");
        List<IngredientsRequest> ingredientsList = new ArrayList<>();

        for (String ingredientString : ingredientsArray) {
            String[] parts = ingredientString.split(",", 2);

            if (parts.length == 2) {
                String name = parts[0].trim();
                String description = parts[1].trim();
                ingredientsList.add(new IngredientsRequest(name, description));
            } else {
                throw new IllegalArgumentException("Invalid ingredient format: " + ingredientString);
            }
        }

        return ingredientsList;
    }
}
