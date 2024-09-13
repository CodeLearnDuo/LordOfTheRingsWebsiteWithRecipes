package blps.duo.dto;

public record TaskResponse(
        String id,
        String name,
        String assignee,
        String processInstanceId
) {
}