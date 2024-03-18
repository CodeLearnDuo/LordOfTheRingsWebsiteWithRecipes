package blps.duo.project.services;

import blps.duo.project.dto.ApiToken;
import blps.duo.project.model.Person;
import blps.duo.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private final PersonRepository personRepository;

    public Mono<Person> getApiTokenOwner(ApiToken apiToken) {
        return personRepository.findById(apiToken.apiToken());
    }
}
