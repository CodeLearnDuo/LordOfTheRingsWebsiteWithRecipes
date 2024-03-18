package blps.duo.project.dto.requests;

import javax.validation.constraints.NotBlank;

public record SingInRequest(
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password
) {
}
