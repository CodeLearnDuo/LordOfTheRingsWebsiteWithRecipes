package blps.duo.project.dto;

public record DeletePersonRequest(
        long id,
        String password,
        String email
) {
}
