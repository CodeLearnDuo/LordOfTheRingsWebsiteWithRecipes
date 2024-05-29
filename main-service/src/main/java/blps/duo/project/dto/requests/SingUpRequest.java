package blps.duo.project.dto.requests;

import javax.validation.constraints.NotBlank;

public record SingUpRequest(
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "Race is required")
        String race
) {
}
