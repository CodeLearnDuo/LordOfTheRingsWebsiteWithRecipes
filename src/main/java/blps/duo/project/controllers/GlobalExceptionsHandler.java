package blps.duo.project.controllers;

import blps.duo.project.dto.responses.ApiErrorResponse;
import blps.duo.project.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(AuthorizationException.class)
    public Mono<ApiErrorResponse> handleAuthorizationException(AuthorizationException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.BAD_REQUEST.value(),
                ex.toString(),
                "Error with Authorization"
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.BAD_REQUEST.value(),
                ex.toString(),
                "Error with Authentication"
        ));
    }

    @ExceptionHandler(NoSuchRecipeException.class)
    public Mono<ApiErrorResponse> handleNoSuchRecipeException(NoSuchRecipeException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.NOT_FOUND.value(),
                ex.toString(),
                "Not exist recipe"
        ));
    }

    @ExceptionHandler(PersonAlreadyExistsException.class)
    public Mono<ApiErrorResponse> handlePersonAlreadyExistsException(PersonAlreadyExistsException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.BAD_REQUEST.value(),
                ex.toString(),
                "Email is already taken"
        ));
    }

    @ExceptionHandler(RaceNotFoundException.class)
    public Mono<ApiErrorResponse> handleRaceNotFoundException(RaceNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.BAD_REQUEST.value(),
                ex.toString(),
                "This race does not exist"
        ));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public Mono<ApiErrorResponse> handlePersonNotFoundException(PersonNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(new ApiErrorResponse(
                Timestamp.valueOf(LocalDateTime.now()),
                exchange.getRequest().getPath().value(),
                HttpStatus.NOT_FOUND.value(),
                ex.toString(),
                "This person does not exist"
        ));
    }






}
