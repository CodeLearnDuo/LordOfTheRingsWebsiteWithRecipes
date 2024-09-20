package blps.duo.workers;

import blps.duo.quartz.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StatisticWorker {

    private final EmailJob emailJob;

    private final ExternalTaskClient client;

    public StatisticWorker(EmailJob emailJob, ExternalTaskClient client) {
        this.emailJob = emailJob;
        this.client = client;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("send-stats-for-all-leaders")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            emailJob.sendEmails();
            externalTaskService.complete(externalTask);
        } catch (Exception e) {
            handleError(e, externalTask, externalTaskService);
        }

    }

    private void handleError(Throwable throwable, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("errorCode", "BP_ERROR");
        variables.put("errorMessage", throwable.getMessage());
        externalTaskService.handleBpmnError(externalTask, "BP_ERROR", throwable.getMessage(), variables);
        log.error("Form data error: {}", throwable.getMessage(), throwable);
    }

}
