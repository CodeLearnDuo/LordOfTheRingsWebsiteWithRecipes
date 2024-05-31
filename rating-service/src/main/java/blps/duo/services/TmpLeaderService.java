package blps.duo.services;

import blps.duo.model.Leader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class TmpLeaderService {

    public Flux<Leader> getLeaders() {
        return Flux.fromIterable(List.of(
                new Leader("qweq", 1L, "123"),
                new Leader("qweq", 1L, "123"),
                new Leader("qweq", 1L, "123"),
                new Leader("qweq", 1L, "123")
        ));
    }
}
