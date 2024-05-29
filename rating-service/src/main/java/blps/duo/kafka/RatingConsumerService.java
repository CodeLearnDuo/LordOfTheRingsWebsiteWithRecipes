package blps.duo.kafka;

import blps.duo.dto.StatisticDTO;
import blps.duo.model.Person;
import blps.duo.model.Recipe;
import blps.duo.model.Statistic;
import blps.duo.repository.PersonRepository;
import blps.duo.repository.RecipeRepository;
import blps.duo.repository.StatisticRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.sql.Timestamp;

@Service
public class RatingConsumerService {

    private final Flux<String> kafkaReceiver;
    private final StatisticRepository statisticRepository;
    private final RecipeRepository recipeRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;
    private final TransactionalOperator requiredNewTransactionalOperator;

    public RatingConsumerService(Flux<String> kafkaReceiver, StatisticRepository statisticRepository,
                                 RecipeRepository recipeRepository, PersonRepository personRepository,
                                 ObjectMapper objectMapper,
                                 TransactionalOperator requiredNewTransactionalOperator) {
        this.kafkaReceiver = kafkaReceiver;
        this.statisticRepository = statisticRepository;
        this.recipeRepository = recipeRepository;
        this.personRepository = personRepository;
        this.objectMapper = objectMapper;
        this.requiredNewTransactionalOperator = requiredNewTransactionalOperator;

        this.kafkaReceiver.subscribe(this::processMessage);

    }

    private static final Logger logger = LoggerFactory.getLogger(RatingConsumerService.class);

    private void processMessage(String message) {

        try {

            StatisticDTO statisticDTO = objectMapper.readValue(message, StatisticDTO.class);

            requiredNewTransactionalOperator.execute(status -> {

                Mono<Recipe> recipeMono = recipeRepository.findById(statisticDTO.recipe().id())
                        .doOnNext(recipe -> logger.info("Found recipe: {}", recipe))
                        .flatMap(recipe -> {
                            recipe.setRating(recipe.getRating() + statisticDTO.rankChange());
                            logger.info("Updated recipe rating: {}", recipe.getRating());
                            return recipeRepository.save(recipe);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            Recipe newRecipe = new Recipe(
                                    statisticDTO.recipe().id(),
                                    statisticDTO.recipe().title(),
                                    statisticDTO.recipe().recipeRaceId(),
                                    statisticDTO.rankChange()
                            );
                            logger.info("Creating new recipe: {}", newRecipe);
                            return recipeRepository.save(newRecipe);
                        }))
                        .doOnSuccess(recipe -> logger.info("Saved recipe: {}", recipe));


                Mono<Person> personMono = personRepository.findById(statisticDTO.person().email())
                        .doOnNext(person -> logger.info("Found person: {}", person))
                        .flatMap(person -> {
                            person.setRatingCount(person.getRatingCount() + 1);
                            logger.info("Updated person rating count: {}", person.getRatingCount());
                            return personRepository.save(person);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            Person newPerson = new Person(
                                    statisticDTO.person().email(),
                                    statisticDTO.person().personRaceId(),
                                    1L
                            );
                            logger.info("Creating new person: {}", newPerson);
                            return personRepository.save(newPerson);
                        }))
                        .doOnSuccess(person -> logger.info("Saved person: {}", person));

                Mono<Void> statisticMono = Mono.zip(recipeMono, personMono)
                        .flatMap(tuple -> {
                            Statistic statistic = new Statistic(
                                    statisticDTO.person().email(),
                                    statisticDTO.recipe().id(),
                                    statisticDTO.rankChange(),
                                    new Timestamp(statisticDTO.timestamp())
                            );
                            logger.info("Creating statistic: {}", statistic);
                            return statisticRepository.save(statistic).then();
                        });

                return statisticMono
                        .doOnSuccess(aVoid -> logger.info("Transaction completed successfully"))
                        .doOnError(e -> logger.error("Transaction failed", e));
            }).subscribeOn(Schedulers.boundedElastic()).subscribe();

        } catch (IOException e) {
            logger.error("Error processing message", e);
        }
    }
}
