package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.PersonResponse;
import blps.duo.project.dto.SingUpRequest;
import blps.duo.project.exceptions.PersonAlreadyExistsException;
import blps.duo.project.exceptions.RaceNotFoundException;
import blps.duo.project.model.Person;
import blps.duo.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PersonService {


    private final PersonRepository personRepository;
    private final RaceService raceService;
    private final PasswordEncoderService passwordEncoderService;

    public Mono<PersonResponse> getPersonById(ApiToken apiToken) {
        return personRepository
                .findById(apiToken.apiToken())
                .flatMap(p ->
                        raceService.getRaceResponseById(p.getPersonRaceId())
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
                        raceService.getRaceResponseById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getEmail(),
                                        p.getUsername(),
                                        race,
                                        p.isALeader())
                                )
                );
    }

    @Transactional
    public Mono<ApiToken> singUp(SingUpRequest singUpRequest) {
        return personRepository
                .existsByEmail(singUpRequest.email())
                .flatMap(alreadyExists -> {
                    if (alreadyExists) {
                        return Mono.error(new PersonAlreadyExistsException());
                    } else {
                        return raceService
                                .getRaceByRaceName(singUpRequest.race())
                                .switchIfEmpty(Mono.error(new RaceNotFoundException()))
                                .flatMap(race ->
                                        Mono.fromFuture(passwordEncoderService.encodePassword(singUpRequest.password()))
                                                .flatMap(hashedPassword ->
                                                        personRepository.save(
                                                                new Person(
                                                                        singUpRequest.email(),
                                                                        singUpRequest.username(),
                                                                        hashedPassword,
                                                                        race.getId()
                                                                )
                                                        )).map(person -> new ApiToken(person.getId()))
                                );
                    }
                });
    }
}
