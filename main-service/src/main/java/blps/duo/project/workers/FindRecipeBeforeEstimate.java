package blps.duo.project.workers;

import blps.duo.project.repositories.RecipeRepository;
import blps.duo.project.services.MinioService;
import blps.duo.project.services.RecipeService;
import blps.duo.project.util.RecipeFileHelper;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class FindRecipeBeforeEstimate {

    private final ExternalTaskClient client;
    private final MinioService minioService;
    private final RecipeRepository recipeRepository;
    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);

    public FindRecipeBeforeEstimate(MinioService minioService, RecipeRepository recipeRepository, ExternalTaskClient client) {
        this.minioService = minioService;
        this.recipeRepository = recipeRepository;
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

        recipeRepository.findByTitle(recipeName)
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found")))
                .flatMap(recipe -> {
                    Map<String, Object> variables = new HashMap<>();

                    variables.put("recipeId", recipe.getId());
                    variables.put("recipeTitle", recipe.getTitle());
                    variables.put("recipeRank", recipe.getRank());

                    String fileName = extractFileNameFromUrl(recipe.getLogoUrl());

                    return sendRecipeLogoToCamunda(recipe.getId(), fileName)
                            .flatMap(fileValue -> {
                                variables.put("recipeLogo", fileValue);
                                externalTaskService.complete(externalTask, variables);
                                return Mono.empty();
                            });
                })
                .onErrorResume(error -> {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("errorMessage", error.getMessage());
                    externalTaskService.handleFailure(externalTask, error.getMessage(), error.getMessage(), 0, 0);
                    return Mono.empty();
                })
                .subscribe();
    }

    private String extractFileNameFromUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public Mono<FileValue> sendRecipeLogoToCamunda(Long recipeId, String logoUrl) {
        return minioService.downloadFileOrDefault(logoUrl)
                .collectList()
                .flatMap(fileBuffers -> {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    fileBuffers.forEach(buffer -> {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        try {
                            outputStream.write(bytes);
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing to output stream", e);
                        }
                    });

                    byte[] fileContent = outputStream.toByteArray();

                    logger.info("Downloaded file size: {}", fileContent.length);
                    if (fileContent.length == 0) {
                        logger.error("File content is empty!");
                    }

                    String mimeType = "image/jpeg";
                    String filename = "recipe-" + recipeId + "-logo.jpg";

                    FileValue fileValue = RecipeFileHelper.buildFile(filename, fileContent, mimeType);

                    return Mono.just(fileValue);
                });
    }
}