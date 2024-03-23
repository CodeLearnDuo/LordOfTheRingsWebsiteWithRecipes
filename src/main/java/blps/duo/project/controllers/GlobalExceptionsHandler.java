package blps.duo.project.controllers;

import blps.duo.project.dto.responses.ApiErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    public ApiErrorResponse handle() {
        return null;
    }
}
