package blps.duo.project.dto.requests;

public record IngredientsRequest(
        String name,
        String description,
        String weight
) {
}
