package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.PersonResponse;
import blps.duo.project.dto.SingInRequest;
import blps.duo.project.dto.SingUpRequest;
import blps.duo.project.services.PersonService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
@Data
public class PersonController {

    private PersonService personService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<PersonResponse> getPerson(@RequestHeader ApiToken apiToken) {
        //TODO validation
        return personService.getPersonById(apiToken);
    }

    @GetMapping("/all")
    public Flux<PersonResponse> getAllPersons() {
        return personService.getAllPersons();
    }

    @PostMapping("/signUp")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiToken> signUp(@RequestBody SingUpRequest singUpRequest) {
        //TODO validation
        return personService.singUp(singUpRequest);
    }

    @PostMapping("/singIn")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiToken> singIn(@RequestBody SingInRequest singInRequest) {
        //TODO validation
        return personService.singIn(singInRequest);
    }
}
