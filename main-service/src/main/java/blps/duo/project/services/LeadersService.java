package blps.duo.project.services;

import blps.duo.project.dto.statistic.StatisticLeaderDTO;
import blps.duo.project.repositories.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@RequiredArgsConstructor
@Service
@Slf4j
public class LeadersService {

    private final PersonRepository personRepository;
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;
    private final RaceService raceService;

    @PostConstruct
    public void updateLeaders() {
        personRepository.findAllLeaders()
                .flatMap(leader -> raceService.getRaceById(leader.getPersonRaceId())
                        .map(race -> new StatisticLeaderDTO(
                                leader.getEmail(),
                                leader.getPersonRaceId(),
                                race.getName()
                        ))
                )
                .collectList()
                .flatMapMany(leaderDtoList -> {
                    String statisticLeaderDTOJson;

                    try {
                        statisticLeaderDTOJson = objectMapper.writeValueAsString(leaderDtoList);
                    } catch (Exception e) {
                        return Flux.error(new RuntimeException("Error serializing statisticLeaderDTO", e));
                    }

                    log.info("SENT MESSAGE: {}", statisticLeaderDTOJson);


                    return kafkaSender.send(Flux.just(SenderRecord.create(
                            new ProducerRecord<>("leader-topic", statisticLeaderDTOJson), null)));
                })
                .doOnNext(record -> log.info("Sent record: {}", record))
                .subscribe();
    }
}
