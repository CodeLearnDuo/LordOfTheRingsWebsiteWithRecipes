package blps.duo.project.service;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.DeletePersonRequest;
import blps.duo.project.dto.PersonResponse;
import blps.duo.project.model.Person;
import blps.duo.project.repositories.PersonRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Service
@Data
@RequiredArgsConstructor
public class PersonService {

    private PersonRepository personRepository;

    public Mono<PersonResponse> findPersonById(ApiToken apiToken) {
        Long id = apiToken.apiToken();
        Mono<Person> found_person = personRepository.findById(id);
        Mono<PersonResponse> = new PersonResponse(

        )

    }

    public Mono<Void> deletePerson(DeletePersonRequest request) {
//        return personService.findPersonById(request)
//                .flatMap(person ->
//                        personService.deletePerson(person)
//                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
//                )
//                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
