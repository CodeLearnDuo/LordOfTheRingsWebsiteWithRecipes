package blps.duo.project.workers;

import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.services.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserRegistrationWorker {

    private final ExternalTaskClient client;
    private final PersonService personService;

    public UserRegistrationWorker(PersonService personService, ExternalTaskClient client) {
        this.personService = personService;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("user-registration")
                .handler((externalTask, externalTaskService) -> {
                    String email = externalTask.getVariable("email_field");
                    String username = externalTask.getVariable("username_field");
                    String password = externalTask.getVariable("password_field");
                    String race = externalTask.getVariable("race_field");

                    SingUpRequest singUpRequest = new SingUpRequest(email, username, password, race);
                    System.out.println(singUpRequest);
                    personService.singUp(singUpRequest)
                            .doOnSuccess(apiToken -> {
                                Map<String, Object> variables = new HashMap<>();
                                variables.put("signinStatus", "success");
                                variables.put("token", apiToken.apiToken());
                                externalTaskService.complete(externalTask, variables);
                            })
                            .doOnError(error -> {
                                handleError(error, externalTask, externalTaskService);
                            })
                            .subscribe();
                })
                .open();
    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("errorCode", "BP_ERROR");
        variables.put("errorMessage", throwable.getMessage());
        externalTaskService.handleBpmnError(externalTask, "BP_ERROR", throwable.getMessage(), variables);
        log.error("Form data error: {}", throwable.getMessage(), throwable);
    }
}
