package blps.duo.project.workers;

import blps.duo.project.dto.requests.SingUpRequest;
import blps.duo.project.services.PersonService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserRegistrationWorker {

    private final ExternalTaskClient client;
    private final PersonService personService;

    public UserRegistrationWorker(PersonService personService, ExternalTaskClient client) {
        this.personService = personService;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        System.out.println("subscribeToTask!!!!!!!!");
        client.subscribe("user-registration")
                .handler((externalTask, externalTaskService) -> {
                    // Получаем данные из переменных процесса
                    String email = externalTask.getVariable("email_field");
                    String username = externalTask.getVariable("username_field");
                    String password = externalTask.getVariable("password_field");
                    String race = externalTask.getVariable("race_field");

                    // Создаем объект запроса на регистрацию
                    SingUpRequest singUpRequest = new SingUpRequest(email, username, password, race);
                    System.out.println(singUpRequest);
                    // Вызываем метод регистрации
                    personService.singUp(singUpRequest)
                            .doOnSuccess(apiToken -> {
                                // Успешная регистрация
                                Map<String, Object> variables = new HashMap<>();
                                variables.put("signinStatus", "success");
                                variables.put("token", apiToken.apiToken());
                                externalTaskService.complete(externalTask, variables);
                            })
                            .doOnError(error -> {
                                // Ошибка при регистрации
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
