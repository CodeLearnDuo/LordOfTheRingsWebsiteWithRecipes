package blps.duo.project.dto;

import javax.validation.constraints.NotBlank;

public record ApiToken(
        @NotBlank(message = "ApiToken is required")
        String apiToken
) {
}
