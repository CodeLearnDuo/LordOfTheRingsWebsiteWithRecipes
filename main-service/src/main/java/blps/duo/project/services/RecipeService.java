package blps.duo.project.services;

import blps.duo.project.dto.requests.AddRecipeRequest;
import blps.duo.project.dto.requests.ScoreRequest;
import blps.duo.project.dto.responses.AddRecipeResponse;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.dto.responses.RecipeResponse;
import blps.duo.project.dto.responses.ShortRecipeResponse;
import blps.duo.project.dto.statistic.StatisticDTO;
import blps.duo.project.dto.statistic.StatisticPerson;
import blps.duo.project.dto.statistic.StatisticRecipe;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.exceptions.JwtAuthenticationException;
import blps.duo.project.exceptions.NoSuchRecipeException;
import blps.duo.project.model.Person;
import blps.duo.project.model.RaceRelationship;
import blps.duo.project.model.Recipe;
import blps.duo.project.model.Statistic;
import blps.duo.project.repositories.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RaceService raceService;
    private final IngredientService ingredientService;
    private final RaceRelationshipsService raceRelationshipsService;
    private final StatisticService statisticService;
    private final PersonService personService;
    private final TransactionalOperator requiredTransactionalOperator;
    private final TransactionalOperator requiredNewTransactionalOperator;
    private final TransactionalOperator requiresNewReadCommitedTransactionalOperator;
    private final MinioService minioService;
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;

    public Mono<RecipeResponse> getRecipeResponseById(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        ingredientService.getAllRecipesIngredients(recipeId)
                                                .collectList()
                                                .flatMap(ingredients ->
                                                        minioService.downloadFile(extractFileNameFromUrl(recipe.getLogoUrl()))
                                                                .collectList()
                                                                .defaultIfEmpty(Collections.emptyList())
                                                                .map(dataList -> {
                                                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                                    dataList.forEach(buffer -> {
                                                                        byte[] bytes = new byte[buffer.remaining()];
                                                                        buffer.get(bytes);
                                                                        try {
                                                                            outputStream.write(bytes);
                                                                        } catch (IOException e) {
                                                                            throw new RuntimeException(e);
                                                                        }
                                                                    });
                                                                    return new RecipeResponse(
                                                                            recipe.getId(),
                                                                            recipe.getTitle(),
                                                                            recipe.getDescription(),
                                                                            outputStream.toByteArray(),
                                                                            new RaceResponse(raceResponse.getName()),
                                                                            ingredients,
                                                                            recipe.getRank()
                                                                    );
                                                                })
                                                )
                                )
                );
    }

    public Flux<ShortRecipeResponse> getAllShortRecipes() {
        return recipeRepository.findAll()
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(raceResponse ->
                                        minioService.downloadFile(extractFileNameFromUrl(recipe.getLogoUrl()))
                                                .collectList()
                                                .defaultIfEmpty(Collections.emptyList())
                                                .map(dataList -> {
                                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                    dataList.forEach(buffer -> {
                                                        byte[] bytes = new byte[buffer.remaining()];
                                                        buffer.get(bytes);
                                                        try {
                                                            outputStream.write(bytes);
                                                        } catch (IOException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    });
                                                    return new ShortRecipeResponse(
                                                            recipe.getId(),
                                                            recipe.getTitle(),
                                                            outputStream.toByteArray(),
                                                            new RaceResponse(raceResponse.getName()),
                                                            recipe.getRank()
                                                    );
                                                })
                                )
                );
    }

    public Mono<AddRecipeResponse> addRecipe(Mono<Person> requestOwnerMono, AddRecipeRequest addRecipeRequest, Mono<FilePart> logoFileMono) {
        return requiresNewReadCommitedTransactionalOperator.transactional(
                requestOwnerMono
                .switchIfEmpty(Mono.error(new AuthorizationException("Unauthorized Access")))
                .flatMap(requestOwner -> {
                    return raceService.existsRaceById(requestOwner.getPersonRaceId())
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid race in jwt")))
                            .then(logoFileMono)
                            .flatMap(logoFile -> {
                                return minioService.uploadLogo(logoFile)
                                        .flatMap(logoId -> {
                                            Recipe newRecipe = new Recipe(
                                                    addRecipeRequest.title(),
                                                    addRecipeRequest.description(),
                                                    logoId,
                                                    requestOwner.getPersonRaceId()
                                            );
                                            return recipeRepository.save(newRecipe)
                                                    .onErrorResume(e -> {
                                                        try {
                                                            return minioService.deleteLogo(logoId).then(Mono.error(e));
                                                        } catch (Throwable throwable) {
                                                            log.error("PANIC in minio rollback", throwable);
                                                            return Mono.error(throwable);
                                                        }
                                                    })
                                                    .flatMap(recipe ->
                                                            ingredientService.saveAllIngredientsForRecipeId(recipe.getId(), addRecipeRequest.ingredients())
                                                                    .onErrorResume(e -> minioService.deleteLogo(logoId).then(Mono.error(e)))
                                                                    .collectList()
                                                                    .map(ingredientsResponseList -> new AddRecipeResponse(
                                                                            recipe.getId(),
                                                                            recipe.getTitle(),
                                                                            recipe.getDescription(),
                                                                            logoId,
                                                                            new RaceResponse(requestOwner.getName()),
                                                                            ingredientsResponseList,
                                                                            recipe.getRank()
                                                                    ))
                                                    );
                                        });
                            });
                })
        );
    }


    public Flux<ShortRecipeResponse> findRecipeByName(String recipeName) {
        final Pattern pattern = Pattern.compile(recipeName);

        return recipeRepository.findAll()
                .filter(recipe -> pattern.matcher(recipe.getTitle()).find())
                .flatMap(recipe ->
                        raceService.getRaceById(recipe.getRaceId())
                                .flatMap(race ->
                                        minioService.downloadFile(extractFileNameFromUrl(recipe.getLogoUrl()))
                                                .collectList()
                                                .map(byteBuffers -> {
                                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                    byteBuffers.forEach(buffer -> {
                                                        while (buffer.hasRemaining()) {
                                                            outputStream.write(buffer.get());
                                                        }
                                                    });
                                                    return new ShortRecipeResponse(
                                                            recipe.getId(),
                                                            recipe.getTitle(),
                                                            outputStream.toByteArray(),
                                                            new RaceResponse(race.getName()),
                                                            recipe.getRank()
                                                    );
                                                })
                                )
                );
    }


    public Mono<RecipeResponse> estimate(Mono<Person> requestOwnerMono, ScoreRequest scoreRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        record OwnerWithRaceRelationship(Person owner, RaceRelationship relationship) {
        }

        return requiredNewTransactionalOperator.transactional(
                requestOwnerMono
                        .switchIfEmpty(Mono.error(new AuthorizationException()))
                        .flatMap(requestOwner -> personService.getPersonByEmail(requestOwner.getEmail()))
                        .flatMap(requestOwner ->
                                raceRelationshipsService.findRelationshipByIds(requestOwner.getId(), scoreRequest.recipeId())
                                        .map(relationship -> new OwnerWithRaceRelationship(requestOwner, relationship))
                        )
                        .flatMap(ownerWithRaceRelationship -> {
                                    var foundEstimationMono = statisticService.findEstimate(ownerWithRaceRelationship.owner.getId(), scoreRequest.recipeId());
                                    return updateRank(
                                            ownerWithRaceRelationship.owner.getId(),
                                            scoreRequest,
                                            ownerWithRaceRelationship.relationship,
                                            foundEstimationMono,
                                            timestamp
                                    ).flatMap(recipeWithRankChange -> {
                                        Recipe recipe = recipeWithRankChange.recipe();
                                        double rankChange = recipeWithRankChange.rankChange();

                                        StatisticPerson statisticPerson = new StatisticPerson(ownerWithRaceRelationship.owner.getEmail(), ownerWithRaceRelationship.owner.getPersonRaceId());
                                        StatisticRecipe statisticRecipe = new StatisticRecipe(recipe.getId(), recipe.getTitle(), recipe.getRaceId());

                                        StatisticDTO statisticDTO = new StatisticDTO(statisticPerson, statisticRecipe, rankChange, timestamp.getTime());

                                        String statisticDTOJson;

                                        try {
                                            statisticDTOJson = objectMapper.writeValueAsString(statisticDTO);
                                        } catch (Exception e) {
                                            return Mono.error(new RuntimeException("Error serializing StatisticDTO", e));
                                        }

                                        return kafkaSender.send(Mono.just(SenderRecord.create(new ProducerRecord<>("ratings-topic", statisticDTOJson), null)))
                                                .then()
                                                .thenReturn(recipe);
                                    });
                                }
                        )
                        .flatMap(recipe -> getRecipeResponseById(recipe.getId()))
        );
    }


    private record RecipeWithRankChange(Recipe recipe, double rankChange) {}

    private Mono<RecipeWithRankChange> updateRank(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Mono<Statistic> foundEstimationMono, Timestamp timestamp) {
        return requiredNewTransactionalOperator.transactional(
                foundEstimationMono
                        .flatMap(foundEstimation -> {
                            if (foundEstimation.isValue() == scoreRequest.value()) {
                                return updateRankByRemovingEstimate(ownerId, scoreRequest, raceRelationship, foundEstimation);
                            } else {
                                return updateRankByChangingEstimate(ownerId, scoreRequest, raceRelationship, foundEstimation, timestamp);
                            }
                        })
                        .switchIfEmpty(updateRankByAddingEstimate(ownerId, scoreRequest, raceRelationship, timestamp))
        );
    }

    private Mono<RecipeWithRankChange> updateRankByRemovingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation) {

        double estimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredNewTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() - estimation);
                            return recipeRepository.save(recipe)
                                    .map(savedRecipe -> new RecipeWithRankChange(savedRecipe, -estimation));
                        })
                        .flatMap(recipeWithRankChange -> statisticService.removeEstimate(ownerId, scoreRequest.recipeId())
                                .thenReturn(recipeWithRankChange))
        );
    }

    private Mono<RecipeWithRankChange> updateRankByChangingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Statistic foundEstimation, Timestamp timestamp) {

        double currentEstimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());
        double previousEstimation = foundEstimation.isValue() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());
        double rankChange = currentEstimation - previousEstimation;

        return requiredNewTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() - previousEstimation + currentEstimation);
                            return recipeRepository.save(recipe)
                                    .map(savedRecipe -> new RecipeWithRankChange(savedRecipe, rankChange));
                        })
                        .flatMap(recipeWithRankChange -> statisticService.collectData(ownerId, scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                                .thenReturn(recipeWithRankChange))
        );
    }

    private Mono<RecipeWithRankChange> updateRankByAddingEstimate(Long ownerId, ScoreRequest scoreRequest, RaceRelationship raceRelationship, Timestamp timestamp) {

        double estimation = scoreRequest.value() ? raceRelationship.getRelationshipCoefficient() : -1 * (1 - raceRelationship.getRelationshipCoefficient());

        return requiredNewTransactionalOperator.transactional(
                recipeRepository.findById(scoreRequest.recipeId())
                        .switchIfEmpty(Mono.error(new NoSuchRecipeException()))
                        .flatMap(recipe -> {
                            recipe.setRank(recipe.getRank() + estimation);
                            return recipeRepository.save(recipe)
                                    .map(savedRecipe -> new RecipeWithRankChange(savedRecipe, estimation));
                        })
                        .flatMap(recipeWithRankChange -> statisticService.collectData(ownerId, scoreRequest.recipeId(), scoreRequest.value(), timestamp)
                                .thenReturn(recipeWithRankChange))
        );
    }


    private String extractFileNameFromUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }


}
