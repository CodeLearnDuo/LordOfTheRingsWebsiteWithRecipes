package blps.duo.project.services;

import blps.duo.project.dto.RaceResponse;
import blps.duo.project.repositories.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RaceService {

    private RaceRepository raceRepository;

    public Mono<RaceResponse> getRaceById(Long id) {
        return raceRepository
                .findById(id)
                .map(race -> new RaceResponse(race.getName()));
    }
}
