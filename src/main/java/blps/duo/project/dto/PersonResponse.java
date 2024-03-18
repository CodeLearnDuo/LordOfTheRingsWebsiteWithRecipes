package blps.duo.project.dto;

public record PersonResponse(
        Long id,
        String username,
        RaceResponse raceName,
        boolean aLeader
) {
}
