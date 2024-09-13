package blps.duo.services;

import blps.duo.dto.TaskResponse;
import blps.duo.dto.UserProfileResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AssigneeService {

    private final WebClient webClient;

    @Value("${camunda.rest.url}")
    private String camundaRestUrl;

    public AssigneeService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(camundaRestUrl).build();
    }

    public Mono<String> getAssigneeEmail(String processInstanceId) {
        String taskUrl = "/task?processInstanceId=" + processInstanceId;

        return webClient.get()
                .uri(taskUrl)
                .retrieve()
                .bodyToMono(TaskResponse[].class)
                .flatMap(tasks -> {
                    if (tasks != null && tasks.length > 0) {
                        String assignee = tasks[0].assignee();
                        if (assignee != null) {
                            String profileUrl = "/user/" + assignee + "/profile";
                            return webClient.get()
                                    .uri(profileUrl)
                                    .retrieve()
                                    .bodyToMono(UserProfileResponse.class)
                                    .map(UserProfileResponse::email)
                                    .switchIfEmpty(Mono.error(new RuntimeException("Профиль пользователя не найден для assignee: " + assignee)));
                        } else {
                            return Mono.error(new RuntimeException("Assignee не назначен для задачи процесса: " + processInstanceId));
                        }
                    } else {
                        return Mono.error(new RuntimeException("Задачи не найдены для процесса: " + processInstanceId));
                    }
                });
    }
}