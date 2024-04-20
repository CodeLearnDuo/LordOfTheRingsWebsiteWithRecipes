package blps.duo.project.repositories.redis;

import blps.duo.project.model.Logo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RedisRepositoryImpl implements RedisRepository {

    private static final String KEY = "Logo";
    private RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, Logo> hashOperations;

    @Autowired
    public RedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public void addLogo(final Logo logo) {
        hashOperations.put(KEY, logo.getId(), logo);
    }

    public Mono<Logo> findLogo(final String id) {
        return Mono.justOrEmpty(hashOperations.get(KEY, id));
    }

}
