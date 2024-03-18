package blps.duo.project.dto.requests;

public record SingInRequest(
        String email,
        String password
) {
}
