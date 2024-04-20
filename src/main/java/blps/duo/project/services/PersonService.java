package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.dto.requests.DeletePersonRequest;
import blps.duo.project.dto.requests.SingInRequest;
import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.dto.responses.PersonResponse;
import blps.duo.project.dto.responses.RaceResponse;
import blps.duo.project.exceptions.*;
import blps.duo.project.model.Person;
import blps.duo.project.model.Race;
import blps.duo.project.repositories.PersonRepository;
import blps.duo.project.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PersonService implements
        ReactiveUserDetailsService,
        ReactiveUserDetailsPasswordService {


    private final PersonRepository personRepository;
    private final RaceService raceService;
    private final PasswordService passwordService;
    private final TransactionalOperator requiredNewTransactionalOperator;
    private final TokenProvider tokenProvider;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return personRepository
                .findByEmail(username)
                .cast(UserDetails.class);
    }

    @Override
    @Transactional
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        return personRepository
                .findByEmail(user.getUsername())
                .flatMap(p -> {
                    p.setPassword(newPassword);
                    return personRepository.save(p);
                });
    }

    public Mono<PersonResponse> getPersonResponseById(Long id) {
        return personRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new PersonNotFoundException()))
                .flatMap(p ->
                        raceService.getRaceById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getName(),
                                        new RaceResponse(race.getName()),
                                        p.isALeader())
                                )
                );
    }

    public Flux<PersonResponse> getAllPersonResponses() {
        return personRepository
                .findAll()
                .flatMap(p ->
                        raceService.getRaceById(p.getPersonRaceId())
                                .map(race -> new PersonResponse(
                                        p.getId(),
                                        p.getName(),
                                        new RaceResponse(race.getName()),
                                        p.isALeader())
                                )
                );
    }


    public Mono<ApiToken> singUp(SingUpRequest singUpRequest) {
        return requiredNewTransactionalOperator.transactional(
                personRepository
                        .existsByEmail(singUpRequest.email())
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new PersonAlreadyExistsException()))
                        .flatMap(exists -> raceService.getRaceByRaceName(singUpRequest.race()))
                        .switchIfEmpty(Mono.error(new RaceNotFoundException()))
                        .flatMap(race -> encodePasswordAndCreatePerson(singUpRequest, race))
                        .map(tokenProvider::generateToken)
                        .map(ApiToken::new)
        );
    }

    private Mono<Person> encodePasswordAndCreatePerson(SingUpRequest signUpRequest, Race race) {
        return passwordService.passwordEncode(signUpRequest.password())
                .flatMap(hashedPassword -> personRepository.save(
                                new Person(
                                        signUpRequest.email(),
                                        signUpRequest.username(),
                                        hashedPassword,
                                        race.getId()
                                )
                        )
                );
    }


    public Mono<ApiToken> singIn(SingInRequest singInRequest) {
        return personRepository
                .findByEmail(singInRequest.email())
                .switchIfEmpty(Mono.error(new AuthenticationException()))
                .flatMap(person -> passwordService
                        .passwordAuthentication(person.getPassword(), singInRequest.password())
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new AuthorizationException("Invalid password")))
                        .map(valid -> new ApiToken(tokenProvider.generateToken(person))
                        )
                );
    }

    @Transactional
    public Mono<Void> delete(Long personId, Mono<Person> requestOwnerMono, DeletePersonRequest deletePersonRequest) {
        return requiredNewTransactionalOperator.transactional(
                requestOwnerMono
                .switchIfEmpty(Mono.error(new AuthorizationException()))
                .flatMap(requestOwner -> getPersonByEmail(requestOwner.getEmail()))
                .flatMap(requestOwner -> passwordService
                        .passwordAuthentication(requestOwner.getPassword(), deletePersonRequest.password())
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new AuthorizationException("Invalid password")))
                        .map(valid -> requestOwner))
                .flatMap(requestOwner -> deletePersonIfAuthorized(personId, requestOwner))
        );
    }

    private Mono<Void> deletePersonIfAuthorized(Long personId, Person requestOwner) {
        if (requestOwner.isALeader()) {
            return deleteForLeader(personId, requestOwner);
        } else if (Objects.equals(personId, requestOwner.getId())) {
            return personRepository.deleteById(personId);
        } else {
            return Mono.error(new AuthorizationException());
        }
    }

    private Mono<Void> deleteForLeader(Long personId, Person requestOwner) {
        return getPersonById(personId)
                .flatMap(person -> {
                            if (Objects.equals(person.getPersonRaceId(), requestOwner.getPersonRaceId())) {
                                return personRepository.delete(person);
                            } else {
                                return Mono.error(new AuthorizationException("Unauthorized to delete this person"));
                            }
                        }
                );
    }

    public Mono<Person> getPersonById(Long personId) {
        return personRepository.findById(personId)
                .switchIfEmpty(Mono.error(new PersonNotFoundException()));
    }

    public Mono<Person> getPersonByEmail(String email) {
        return personRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new PersonNotFoundException()));
    }
}
