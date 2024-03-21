package blps.duo.project.dto.requests;

import javax.validation.constraints.NotBlank;

public record FindRecipeRequest(

        @NotBlank(message = "name of recipe is required")
        String recipeName
) {
}
