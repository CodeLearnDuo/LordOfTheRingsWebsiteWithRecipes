package blps.duo.project.services;

import blps.duo.project.dto.*;
import blps.duo.project.dto.requests.DeletePersonRequest;
import blps.duo.project.dto.requests.SingInRequest;
import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.dto.responses.PersonResponse;
import blps.duo.project.exceptions.AuthenticationException;
import blps.duo.project.exceptions.AuthorizationException;
import blps.duo.project.exceptions.PersonAlreadyExistsException;
import blps.duo.project.exceptions.RaceNotFoundException;
import blps.duo.project.model.Person;
import blps.duo.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PersonService {


    private final PersonRepository personRepository;
    private final RaceService raceService;
    private final PasswordService passwordService;
    private final ApiTokenService apiTokenService;

    public Mono<PersonResponse> getPersonResponseById(Long id) {
        return personRepository
                .findById(id)
                .flatMap(p ->
                        raceService.getRaceResponseById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getUsername(),
                                        race,
                                        p.isALeader())
                                )
                );
    }

    public Flux<PersonResponse> getAllPersonResponses() {
        return personRepository
                .findAll()
                .flatMap(p ->
                        raceService.getRaceResponseById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
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
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new PersonAlreadyExistsException());
                    } else {
                        return raceService
                                .getRaceByRaceName(singUpRequest.race())
                                .switchIfEmpty(Mono.error(new RaceNotFoundException()))
                                .flatMap(race ->
                                        Mono.fromFuture(passwordService.passwordEncode(singUpRequest.password()))
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

    public Mono<ApiToken> singIn(SingInRequest singInRequest) {
        return personRepository
                .findByEmail(singInRequest.email())
                .switchIfEmpty(Mono.error(new AuthenticationException()))
                .flatMap(person -> Mono.fromFuture(
                                        passwordService.passwordAuthentication(person.getPassword(), singInRequest.password())
                                )
                        .flatMap(solution -> {
                            if (solution) {
                                return Mono.just(new ApiToken(person.getId()));
                            } else {
                                return Mono.error(new AuthenticationException());
                            }
                        })
                );
    }

    @Transactional
    public Mono<Void> delete(Long personId, ApiToken apiToken, DeletePersonRequest deletePersonRequest) {
        return apiTokenService.getApiTokenOwner(apiToken)
                .switchIfEmpty(Mono.error(new AuthorizationException()))
                .flatMap(person -> {
                    if (!Objects.equals(personId, person.getId())) {
                        return Mono.error(new AuthorizationException());
                    } else {
                        return Mono.fromFuture(
                                passwordService.passwordAuthentication(person.getPassword(), deletePersonRequest.password())
                        ).flatMap(solution -> {
                            if (solution) {
                                return personRepository.deleteById(personId);
                            } else {
                                return Mono.error(new AuthorizationException());
                            }
                        });
                    }
                });
    }
}
