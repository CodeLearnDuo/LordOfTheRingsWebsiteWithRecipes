package blps.duo.project.dto.requests;

import javax.validation.constraints.NotBlank;

public record IngredientsRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "description is required")
        String description
){
}

