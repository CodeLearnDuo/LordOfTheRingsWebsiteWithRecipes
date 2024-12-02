package blps.duo.project.services;

import blps.duo.project.dto.responses.CamundaUserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AssigneeService {
    private final WebClient webClient;

    public AssigneeService(
            WebClient.Builder webClientBuilder,
            @Value("${camunda.rest.url}") String camundaRestUrl,
            @Value("${camunda.rest.username}") String camundaUsername,
            @Value("${camunda.rest.password}") String camundaPassword) {

        log.info("!! Creating AssigneeService");
        log.info("!! camundaRestUrl: {}", camundaRestUrl);
        log.info("!! camundaUsername: {}", camundaUsername);
        log.info("!! camundaPassword: {}", camundaPassword);

        this.webClient = webClientBuilder
                .baseUrl(camundaRestUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(camundaUsername, camundaPassword))
                .build();
    }

    public Mono<CamundaUserProfileResponse> getUserByUserId(String userId) {
        String profileUrl = "/user/" + userId + "/profile";
        log.info("Получение профиля пользователя для userId: {}", userId);

        return webClient.get()
                .uri(profileUrl)
                .retrieve()
                .bodyToMono(CamundaUserProfileResponse.class)
                .switchIfEmpty(Mono.error(new RuntimeException("Профиль пользователя не найден для userId: " + userId)));
    }
}