package blps.duo.project.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PasswordService {

    private static final int FACTOR = 12;

    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();

    @Async
    public CompletableFuture<String> passwordEncode(String rawPassword) {
        String encodedPassword = hasher.hashToString(FACTOR, rawPassword.toCharArray());
        return CompletableFuture.completedFuture(encodedPassword);
    }

    @Async
    public  CompletableFuture<Boolean> passwordAuthentication(String excepted, String actual) {
        boolean solution = verifyer.verify(actual.toCharArray(), excepted.toCharArray()).verified;
        return CompletableFuture.completedFuture(solution);
    }
}
