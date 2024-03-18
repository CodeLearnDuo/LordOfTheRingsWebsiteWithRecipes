package blps.duo.project.dto.responses;

import blps.duo.project.dto.responses.RaceResponse;

public record ShortRecipeResponse(
        Long id,
        String title,
        byte[] logo,
        RaceResponse raceName,
        double rank
) {
}
