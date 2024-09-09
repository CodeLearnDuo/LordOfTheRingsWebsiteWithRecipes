package blps.duo.workers;

import blps.duo.quartz.EmailJob;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
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
        emailJob.sendEmails();
        externalTaskService.complete(externalTask);
    }
}
