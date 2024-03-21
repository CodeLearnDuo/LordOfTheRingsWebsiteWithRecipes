package blps.duo.project.services;

import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.exceptions.RaceNotFoundException;
import blps.duo.project.model.Race;
import blps.duo.project.repositories.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;

    public Mono<Race> getRaceById(Long id) {
        return raceRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new RaceNotFoundException()));
    }

    public Mono<Race> getRaceByRaceName(String raceName) {
        return raceRepository.findRaceByName(raceName)
                .switchIfEmpty(Mono.error(new RaceNotFoundException()));
    }
}
