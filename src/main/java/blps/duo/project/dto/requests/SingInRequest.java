package blps.duo.project.dto.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record SingInRequest(

        @Email
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password
) {
}
