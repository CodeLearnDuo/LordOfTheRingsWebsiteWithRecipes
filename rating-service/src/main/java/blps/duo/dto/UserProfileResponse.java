package blps.duo.dto;

public record UserProfileResponse(
    String id,
    String firstName,
    String lastName,
    String email
) {}
