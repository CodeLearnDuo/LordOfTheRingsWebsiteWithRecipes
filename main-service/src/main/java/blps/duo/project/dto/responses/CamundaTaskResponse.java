package blps.duo.project.dto.responses;

public record CamundaTaskResponse(
        String id,
        String name,
        String assignee,
        String processInstanceId
) {
}