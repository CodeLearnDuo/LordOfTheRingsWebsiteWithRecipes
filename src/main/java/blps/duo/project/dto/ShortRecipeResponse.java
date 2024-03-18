package blps.duo.project.dto;

public record ShortRecipeResponse(
        Long id,
        String title,
        byte[] logo,
        RaceResponse raceName,
        double rank
) {
}
