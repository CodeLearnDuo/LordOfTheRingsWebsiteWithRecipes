package blps.duo.workers;

import blps.duo.dto.CamundaUserProfileResponse;
import blps.duo.services.AssigneeService;
import blps.duo.services.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatisticByResponseWorker {

    private static final long ONE_DAY = 86400000;

    private final JavaMailSender mailSender;
    private final ExternalTaskClient client;
    private final AssigneeService assigneeService;
    private final ReportService reportService;


    public StatisticByResponseWorker(JavaMailSender mailSender, ExternalTaskClient client, AssigneeService assigneeService, ReportService reportService) {
        this.mailSender = mailSender;
        this.client = client;
        this.assigneeService = assigneeService;
        this.reportService = reportService;
        subscribeToTask();
    }

    private void subscribeToTask() {
        client.subscribe("send-stats-for-current-leader")
                .handler(this::handleTask)
                .open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        var processDefinitionKey = externalTask.getProcessInstanceId();
        var offsetString = (String) externalTask.getVariable("offset_field");
        var offset = Long.parseLong(offsetString) * ONE_DAY;

        assigneeService
                .getAssigneeUser(processDefinitionKey)
                .map(CamundaUserProfileResponse::email)
                .flatMap(email -> reportService.getSmallReportByLeaderEmailAndOffset(email, offset)
                        .map(reportText -> createEmailMessage(email, reportText, offsetString)))
                .doOnNext(mailSender::send)
                .doOnError(error -> log.error("Произошла ошибка при отправке писем: ", error))
                .subscribe();


        externalTaskService.complete(externalTask);
    }

    private SimpleMailMessage createEmailMessage(String recipientEmail, String reportText, String offsetString) {
        log.info("Создано письмо для: {}", recipientEmail);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Статистика за " + offsetString + " дней");
        message.setText(reportText);
        return message;
    }
}