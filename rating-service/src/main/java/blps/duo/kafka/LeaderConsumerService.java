package blps.duo.kafka;

import blps.duo.dto.StatisticLeaderDTO;
import blps.duo.model.Leader;
import blps.duo.repository.LeaderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
public class LeaderConsumerService {

    private final LeaderRepository leaderRepository;
    private final ObjectMapper objectMapper;

    public LeaderConsumerService(Flux<String> kafkaLeaderReceiver,
                                 LeaderRepository leaderRepository,
                                 ObjectMapper objectMapper) {
        this.leaderRepository = leaderRepository;
        this.objectMapper = objectMapper;
        kafkaLeaderReceiver
                .doOnNext(this::processMessage)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

    }

    private void processMessage(String message) {
        try {
            log.info("MESSAGE RECEIVED: {}", message);

            List<StatisticLeaderDTO> leaderDtoList = objectMapper.readValue(message, new TypeReference<List<StatisticLeaderDTO>>(){});

            leaderRepository.deleteAll()
                    .thenMany(Flux.fromIterable(leaderDtoList)
                            .flatMap(dto -> leaderRepository.save(new Leader(dto.email(), dto.raceId(), dto.raceTitle())))
                    )
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
