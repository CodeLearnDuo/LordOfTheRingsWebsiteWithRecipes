package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
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

import javax.validation.Valid;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
@Data
public class PersonController {

    private final PersonService personService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<PersonResponse> getPerson(@PathVariable Long id) {
        return personService.getPersonResponseById(id);
    }

    @GetMapping("/all")
    public Flux<PersonResponse> getAllPersons() {
        return personService.getAllPersonResponses();
    }

    @PostMapping("/signUp")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiToken> signUp(@RequestBody @Valid SingUpRequest singUpRequest) {
        return personService.singUp(singUpRequest);
    }

    @PostMapping("/singIn")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiToken> singIn(@RequestBody @Valid SingInRequest singInRequest) {
        return personService.singIn(singInRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deletePerson(@PathVariable Long id,
                                   @RequestHeader("ApiToken") @Valid ApiToken apiToken,
                                   @RequestBody @Valid DeletePersonRequest deletePersonRequest) {
        return personService.delete(id, apiToken, deletePersonRequest);
    }
}
