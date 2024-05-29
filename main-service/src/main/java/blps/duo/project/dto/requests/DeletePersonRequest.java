package blps.duo.project.dto.requests;

import javax.validation.constraints.NotBlank;

public record DeletePersonRequest(
        @NotBlank(message = "Password is required")
        String password
) {
}
