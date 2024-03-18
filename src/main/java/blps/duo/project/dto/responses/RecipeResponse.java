package blps.duo.project.dto.responses;

import blps.duo.project.dto.responses.IngredientsResponse;
import blps.duo.project.dto.responses.RaceResponse;

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
