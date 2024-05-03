package blps.duo.project.dto.requests;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public record AddRecipeRequest (
        @NotBlank(message = "title is required")
        String title,

        @NotBlank(message = "description is required")
        String description,

        @NotEmpty(message = "ingredients list cannot be empty")
        @Valid
        List<IngredientsRequest> ingredients
) {
}
