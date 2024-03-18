package blps.duo.project.dto.responses;

public record IngredientsResponse(
        Long id,
        String name,
        String description,
        String weight
) {
}
