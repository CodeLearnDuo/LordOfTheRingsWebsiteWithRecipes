package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.DeletePersonRequest;
import blps.duo.project.dto.requests.SingInRequest;
import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.dto.responses.PersonResponse;
import blps.duo.project.model.Person;
import blps.duo.project.services.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    //all
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<PersonResponse> getPerson(@PathVariable Long id) {
        return personService.getPersonResponseById(id);
    }

    //all
    @GetMapping
    public Flux<PersonResponse> getAllPersons() {
        return personService.getAllPersonResponses();
    }

    //all
    @PostMapping("/signUp")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiToken> signUp(@RequestBody @Valid SingUpRequest singUpRequest) {
        return personService.singUp(singUpRequest);
    }

    //all
    @PostMapping("/singIn")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApiToken> singIn(@RequestBody @Valid SingInRequest singInRequest) {
        return personService.singIn(singInRequest);
    }

    //only user with this id or admin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deletePerson(@PathVariable Long id,
                                   @AuthenticationPrincipal Mono<Person> requestOwnerMono,
                                   @RequestBody @Valid DeletePersonRequest deletePersonRequest) {
        log.info("Deleting person with id {}", id);
        return personService.delete(id, requestOwnerMono, deletePersonRequest);
    }
}
