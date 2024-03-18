package blps.duo.project.dto.requests;

public record SingUpRequest(
        String email,
        String username,
        String password,
        String race
) {
}
