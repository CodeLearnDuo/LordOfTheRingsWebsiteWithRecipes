package blps.duo.project.dto.responses;

import java.util.List;

public record AddRecipeResponse(
        Long id,
        String title,
        String description,
        String logo,
        RaceResponse raceName,
        List<IngredientsResponse> ingredients,
        double rank
) {
}
