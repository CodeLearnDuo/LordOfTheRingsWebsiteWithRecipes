package blps.duo.services;

import blps.duo.model.ExpandedStatistic;
import blps.duo.model.Statistic;
import blps.duo.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

    public Flux<Statistic> getStatisticByRaceIdAndOffset(Long raceId, Long offset) {
        return statisticRepository.findAllByRaceIdSAndSince(raceId, new Timestamp(System.currentTimeMillis() - offset));
    }

    public Flux<ExpandedStatistic> getExpandedStatisticByRaceIdAndOffset(Long raceId, Long offset) {
        return statisticRepository.findExpandedStatisticByRaceIdSAndSince(raceId, new Timestamp(System.currentTimeMillis() - offset));
    }



}
