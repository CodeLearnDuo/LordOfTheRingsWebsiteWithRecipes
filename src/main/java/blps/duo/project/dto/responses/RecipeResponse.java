package blps.duo.project.dto.responses;

import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        String description,
        byte[] logo,
        RaceResponse raceName,
        List<IngredientsResponse> ingredients,
        double rank
) {
}
