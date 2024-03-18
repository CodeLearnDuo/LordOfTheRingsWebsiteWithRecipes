package blps.duo.project.controllers;

import blps.duo.project.dto.*;
import blps.duo.project.dto.requests.DeletePersonRequest;
import blps.duo.project.dto.requests.SingInRequest;
import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.dto.responses.PersonResponse;
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

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<PersonResponse> getPerson(@PathVariable Long id) {
        //TODO validation
        return personService.getPersonResponseById(id);
    }

    @GetMapping("/all")
    public Flux<PersonResponse> getAllPersons() {
        return personService.getAllPersonResponses();
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deletePerson(@PathVariable Long id, @RequestHeader("ApiToken") ApiToken apiToken, @RequestBody DeletePersonRequest deletePersonRequest) {
        //TODO validation
        return personService.delete(id, apiToken, deletePersonRequest);
    }
}
