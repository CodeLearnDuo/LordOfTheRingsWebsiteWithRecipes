package blps.duo.project.dto.responses;

import java.sql.Timestamp;

public record ApiErrorResponse(
        Timestamp timestamp,
        String path,
        int status,
        String error,
        String description
) {
}
