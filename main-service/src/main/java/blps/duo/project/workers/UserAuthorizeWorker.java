package blps.duo.project.workers;

import blps.duo.project.dto.requests.SingInRequest;
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
public class UserAuthorizeWorker {

    private final ExternalTaskClient client;
    private final PersonService personService;

    public UserAuthorizeWorker(PersonService personService, ExternalTaskClient client) {
        this.personService = personService;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("user-authorization")
                .handler((externalTask, externalTaskService) -> {
                    String email = externalTask.getVariable("email_field");
                    String password = externalTask.getVariable("password_field");

                    SingInRequest singInRequest = new SingInRequest(email, password);

                    log.info(String.valueOf(singInRequest));

                    personService.singIn(singInRequest)
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
