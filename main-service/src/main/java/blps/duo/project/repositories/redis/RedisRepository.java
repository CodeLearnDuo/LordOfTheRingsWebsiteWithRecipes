package blps.duo.project.repositories.redis;

import blps.duo.project.model.Logo;
import reactor.core.publisher.Mono;

public interface RedisRepository {

    void addLogo(Logo logo);

    Mono<Logo> findLogo(String id);

}
