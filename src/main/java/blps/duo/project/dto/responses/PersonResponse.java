package blps.duo.project.dto.responses;

public record PersonResponse(
        Long id,
        String username,
        RaceResponse raceName,
        boolean aLeader
) {
}
