package blps.duo.project.dto.responses;

public record CamundaUserProfileResponse(
    String id,
    String firstName,
    String lastName,
    String email
) {}
