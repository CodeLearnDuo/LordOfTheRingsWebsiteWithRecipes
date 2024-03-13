package blps.duo.project.dto;

public record SingUpRequest(
        String email,
        String username,
        String password,
        String race
) {
}
