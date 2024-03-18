package blps.duo.project.dto.requests;

import java.util.List;

public record AddRecipeRequest(
        String title,
        String description,
        byte[] logo,
        List<IngredientsRequest> ingredients
) {
}
