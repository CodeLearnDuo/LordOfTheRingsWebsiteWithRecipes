package blps.duo.project.workers;

import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.IngredientsRequest;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.dto.responses.CamundaUserProfileResponse;
import blps.duo.project.model.Person;
import blps.duo.project.services.AssigneeService;
import blps.duo.project.services.PersonService;
import blps.duo.project.services.RecipeService;
import blps.duo.project.util.CustomFilePart;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component
public class AddRecipeWorker {
    private final RecipeService recipeService;
    private final PersonService personService;
    private final ExternalTaskClient client;
    private final AssigneeService assigneeService;

    private static final Logger logger = LoggerFactory.getLogger(AddRecipeWorker.class);

    public AddRecipeWorker(RecipeService recipeService, PersonService personService, ExternalTaskClient client, AssigneeService assigneeService) {
        this.assigneeService = assigneeService;
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

        logger.info("Processing task with title: {}, description: {}", title, description);

        try {
            Mono<FilePart> logoFileMono = extractFilePart(externalTask);
            List<IngredientsRequest> ingredients = parseIngredients(ingredientsString);
            AddRecipeRequest addRecipeRequest = new AddRecipeRequest(title, description, ingredients);
            Mono<Person> requestOwnerMono = getPersonFromContext(externalTask);

            recipeService.addRecipe(requestOwnerMono, addRecipeRequest, logoFileMono)
                    .doOnSuccess(addRecipeResponse -> {
                        logger.info("Recipe added successfully: {}", addRecipeResponse);
                        handleSuccess(addRecipeResponse, externalTask, externalTaskService);
                    })
                    .doOnError(throwable -> {
                        logger.error("Error while adding recipe: {}", throwable.getMessage(), throwable);
                        handleError(throwable, externalTask, externalTaskService);
                    })
                    .subscribe();
        } catch (Exception e) {
            logger.error("Error while adding recipe: {}", e.getMessage(), e);
            handleError(e, externalTask, externalTaskService);
        }

    }

    private Mono<FilePart> extractFilePart(ExternalTask externalTask) {
        Object fileVariable = externalTask.getVariable("logo_field");

        if (fileVariable != null) {
            logger.info("File variable type: {}", fileVariable.getClass().getName());
        } else {
            logger.warn("File variable 'logo_field' is null.");
        }

        if (fileVariable instanceof ByteArrayInputStream) {
            ByteArrayInputStream fileInputStream = (ByteArrayInputStream) fileVariable;
            String fileName = UUID.randomUUID() + "uploaded-file.jpg";
            String mimeType = "image/jpeg";

            logger.info("File received as ByteArrayInputStream with size: {}", fileInputStream.available());
            return Mono.just(new CustomFilePart(fileName, fileInputStream, mimeType));
        } else if (fileVariable instanceof byte[]) {
            byte[] fileBytes = (byte[]) fileVariable;
            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileBytes);
            String fileName = UUID.randomUUID() + "uploaded-file.jpg";
            String mimeType = "image/jpeg";

            logger.info("File received as byte array, size: {} bytes", fileBytes.length);
            return Mono.just(new CustomFilePart(fileName, fileInputStream, mimeType));
        }

        logger.warn("No valid file received");
        return Mono.empty();
    }


    private Mono<Person> getPersonFromContext(ExternalTask externalTask) {
        String userId = externalTask.getVariable("initiator");

        return assigneeService
                .getUserByUserId(userId)
                .map(CamundaUserProfileResponse::email)
                .flatMap(personService::getPersonByEmail);
    }

    private void handleSuccess(AddRecipeResponse response, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("response", response);
        variables.put("Add status", "success");
        externalTaskService.complete(externalTask, variables);
    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("errorCode", "BP_ERROR");
        variables.put("errorMessage", throwable.getMessage());

        externalTaskService.handleBpmnError(externalTask, "BP_ERROR", throwable.getMessage(), variables);
        logger.error("Form data error: {}", throwable.getMessage(), throwable);
    }

    private static List<IngredientsRequest> parseIngredients(String ingredientsString) {
        if (ingredientsString == null || ingredientsString.isEmpty()) {
            throw new IllegalArgumentException("Ingredients string is empty or null");
        }

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