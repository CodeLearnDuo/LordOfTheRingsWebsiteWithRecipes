package blps.duo.project.workers;

import blps.duo.project.dto.requests.SingInRequest;
import blps.duo.project.services.PersonService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
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

                    System.out.println(singInRequest);

                    personService.singIn(singInRequest)
                            .doOnSuccess(apiToken -> {
                                Map<String, Object> variables = new HashMap<>();
                                variables.put("signinStatus", "success");
                                variables.put("token", apiToken.apiToken());
                                externalTaskService.complete(externalTask, variables);
                            })
                            .doOnError(error -> {
                                Map<String, Object> variables = new HashMap<>();
                                variables.put("signinStatus", "error");
                                variables.put("errorMessage", error.getMessage());
                                externalTaskService.complete(externalTask, variables);
                            })
                            .subscribe();
                })
                .open();
    }
}
