package blps.duo.project.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class PasswordService {

    private static final int FACTOR = 12;

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();

    public Mono<String> passwordEncode(String rawPassword) {
        return Mono.fromCallable(() -> hasher.hashToString(FACTOR, rawPassword.toCharArray()))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("Error encode password", e);
                    return Mono.error(e);
                });
    }

    public Mono<Boolean> passwordAuthentication(String excepted, String actual) {
        return Mono.fromCallable(() -> verifyer.verify(actual.toCharArray(), excepted.toCharArray()).verified)
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("Error authenticating password", e);
                    return Mono.just(false);
                });
    }
}
