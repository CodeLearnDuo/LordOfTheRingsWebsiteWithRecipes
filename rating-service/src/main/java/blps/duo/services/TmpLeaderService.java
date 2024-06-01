package blps.duo.services;

import blps.duo.model.Leader;
import blps.duo.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TmpLeaderService {

    private final LeaderRepository leaderRepository;

    public Flux<Leader> getLeaders() {
        return leaderRepository.findAll();
    }
}
