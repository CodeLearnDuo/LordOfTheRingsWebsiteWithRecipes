package blps.duo.dto;

public record CamundaTaskResponse(
        String id,
        String name,
        String assignee,
        String processInstanceId
) {
}