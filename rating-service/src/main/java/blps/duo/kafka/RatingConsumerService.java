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

    private final Flux<String> kafkaRatingReceiver;
    private final StatisticRepository statisticRepository;
    private final RecipeRepository recipeRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;
    private final TransactionalOperator requiredNewTransactionalOperator;

    public RatingConsumerService(Flux<String> kafkaRatingReceiver, StatisticRepository statisticRepository,
                                 RecipeRepository recipeRepository, PersonRepository personRepository,
                                 ObjectMapper objectMapper,
                                 TransactionalOperator requiredNewTransactionalOperator) {
        this.kafkaRatingReceiver = kafkaRatingReceiver;
        this.statisticRepository = statisticRepository;
        this.recipeRepository = recipeRepository;
        this.personRepository = personRepository;
        this.objectMapper = objectMapper;
        this.requiredNewTransactionalOperator = requiredNewTransactionalOperator;

        this.kafkaRatingReceiver.subscribe(this::processMessage);

    }

    private static final Logger logger = LoggerFactory.getLogger(RatingConsumerService.class);

    private void processMessage(String message) {
        logger.info("Received message: {}", message);

        try {
            StatisticDTO statisticDTO = objectMapper.readValue(message, StatisticDTO.class);
            logger.info("Parsed StatisticDTO: {}", statisticDTO);

            requiredNewTransactionalOperator.execute(status -> {
                logger.info("Starting transaction");

                Mono<Recipe> recipeMono = recipeRepository.findByRecipeId(statisticDTO.recipe().id())
                        .switchIfEmpty(Mono.defer(() -> {
                            Recipe newRecipe = new Recipe(
                                    statisticDTO.recipe().id(),
                                    statisticDTO.recipe().title(),
                                    statisticDTO.recipe().recipeRaceId(),
                                    0.0
                            );
                            return recipeRepository.save(newRecipe);
                        }))
                        .flatMap(recipe -> {
                            recipe.setRating(recipe.getRating() + statisticDTO.rankChange());
                            return recipeRepository.save(recipe);
                        })
                        .doOnNext(recipe -> logger.info("Processed Recipe: {}", recipe))
                        .doOnError(e -> logger.error("Error processing recipe", e));

                Mono<Person> personMono = personRepository.findByEmail(statisticDTO.person().email())
                        .flatMap(person -> {
                            person.setRatingCount(person.getRatingCount() + 1);
                            return personRepository.save(person);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            Person newPerson = new Person(
                                    statisticDTO.person().email(),
                                    statisticDTO.person().personRaceId(),
                                    1L
                            );
                            return personRepository.save(newPerson);
                        }))
                        .doOnNext(person -> logger.info("Processed Person: {}", person))
                        .doOnError(e -> logger.error("Error processing person", e));

                Mono<Void> statisticMono = Mono.zip(recipeMono, personMono)
                        .flatMap(tuple -> {
                            Statistic statistic = new Statistic(
                                    statisticDTO.person().email(),
                                    statisticDTO.recipe().id(),
                                    statisticDTO.rankChange(),
                                    new Timestamp(statisticDTO.timestamp())
                            );
                            return statisticRepository.save(statistic).then();
                        })
                        .doOnError(e -> logger.error("Error in transaction composition", e));

                return statisticMono
                        .doOnSuccess(aVoid -> logger.info("Transaction completed successfully"))
                        .doOnError(e -> logger.error("Transaction failed", e));
            }).subscribeOn(Schedulers.boundedElastic()).subscribe();

        } catch (IOException e) {
            logger.error("Error processing message", e);
        }
    }
}
