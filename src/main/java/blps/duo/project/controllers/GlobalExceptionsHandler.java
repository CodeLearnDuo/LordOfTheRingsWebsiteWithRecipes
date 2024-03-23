package blps.duo.project.controllers;

import blps.duo.project.dto.responses.ApiErrorResponse;
import blps.duo.project.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(AuthorizationException.class)
    public Mono<ServerResponse> handleAuthorizationException(AuthorizationException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.BAD_REQUEST.value(),
                        ex.toString(),
                        "Error with Authorization"
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ServerResponse> handleAuthenticationException(AuthenticationException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.BAD_REQUEST.value(),
                        ex.toString(),
                        "Error with Authentication"
                ));
    }

    @ExceptionHandler(NoSuchRecipeException.class)
    public Mono<ServerResponse> handleNoSuchRecipeException(NoSuchRecipeException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.NOT_FOUND.value(),
                        ex.toString(),
                        "Not exist recipe"
                ));
    }

    @ExceptionHandler(PersonAlreadyExistsException.class)
    public Mono<ServerResponse> handlePersonAlreadyExistsException(PersonAlreadyExistsException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.BAD_REQUEST.value(),
                        ex.toString(),
                        "Email is already taken"
                ));
    }

    @ExceptionHandler(RaceNotFoundException.class)
    public Mono<ServerResponse> handleRaceNotFoundException(RaceNotFoundException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.BAD_REQUEST.value(),
                        ex.toString(),
                        "This race does not exist"
                ));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public Mono<ServerResponse> handlePersonNotFoundException(PersonNotFoundException ex, ServerWebExchange exchange) {
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApiErrorResponse(
                        Timestamp.valueOf(LocalDateTime.now()),
                        exchange.getRequest().getPath().value(),
                        HttpStatus.NOT_FOUND.value(),
                        ex.toString(),
                        "This person does not exist"
                ));
    }






}
