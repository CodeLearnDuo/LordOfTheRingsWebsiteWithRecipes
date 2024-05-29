package blps.duo.project.dto.requests;

import javax.validation.constraints.NotNull;

public record ScoreRequest(
        @NotNull
        boolean value,
        @NotNull
        Long recipeId
) {
}
