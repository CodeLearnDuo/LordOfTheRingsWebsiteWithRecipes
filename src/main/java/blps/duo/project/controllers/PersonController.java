package blps.duo.project.controllers;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.DeletePersonRequest;
import blps.duo.project.dto.PersonResponse;
import blps.duo.project.dto.SingUpRequest;
import blps.duo.project.services.PersonService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Flux<PersonResponse> getAllPersons(String name) {
//        return personService.findStudentsByName(name);
        return null;
    }

    @PostMapping
    public Mono<PersonResponse> registerPerson(@RequestBody SingUpRequest request) {
//        return personService.addNewStudent(singUpRequest);
        return null;
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteStudent(@RequestBody DeletePersonRequest request) {
//        return personService.findPersonById(request)
//                .flatMap(person ->
//                        personService.deletePerson(person)
//                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
//                )
//                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        return null;
    }





}
