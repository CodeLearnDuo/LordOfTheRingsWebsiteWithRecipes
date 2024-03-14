package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.DeletePersonRequest;
import blps.duo.project.dto.PersonResponse;
import blps.duo.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PersonService {

    private PersonRepository personRepository;
    private RaceService raceService;

    public Mono<PersonResponse> getPersonById(ApiToken apiToken) {
        return personRepository
                .findById(apiToken.apiToken())
                .flatMap(p ->
                        raceService.getRaceById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getEmail(),
                                        p.getUsername(),
                                        race,
                                        p.isALeader())
                                )
                );
    }

    public Flux<PersonResponse> getAllPersons() {
        return personRepository
                .findAll()
                .flatMap(p ->
                        raceService.getRaceById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getEmail(),
                                        p.getUsername(),
                                        race,
                                        p.isALeader())
                                )
                );
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
