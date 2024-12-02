package blps.duo.workers;

import blps.duo.services.AssigneeService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestWorker {

    private final ExternalTaskClient client;
    private final AssigneeService assigneeService;

    public TestWorker(ExternalTaskClient client, AssigneeService assigneeService) {
        this.client = client;
        this.assigneeService = assigneeService;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("test-topic")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        var processInstanceId = externalTask.getProcessInstanceId();
        log.info("!!Process instance id {}", processInstanceId);
        var taskId = externalTask.getId();
        log.info("!!Task id {}", taskId);
        String initiator = externalTask.getVariable("initiator");
        log.info("!!Initiator {}", initiator);
        var user = assigneeService.getUserByUserId(initiator);
        log.info("!!User {}", user.block());
        externalTaskService.complete(externalTask);
    }
}
