package blps.duo.project.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PasswordEncoderService {

    private static final int FACTOR = 12;

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();

    @Async
    public CompletableFuture<String> encodePassword(String rawPassword) {
        String encodedPassword = hasher.hashToString(FACTOR, rawPassword.toCharArray());
        return CompletableFuture.completedFuture(encodedPassword);
    }
}
