package blps.duo.quartz;

import blps.duo.services.ReportService;
import blps.duo.services.TmpLeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailJob implements Job {

    private static final long ONE_MONTH = 2629800000L;

    private final JavaMailSender mailSender;
    private final ReportService reportService;
    private final TmpLeaderService tmpLeaderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        sendEmails();
    }

    public void sendEmails() {
        tmpLeaderService.getLeaders()
                .map(leader -> {
                    System.out.println(leader);
                    return leader;
                })
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("Список лидеров пуст. Нет писем для отправки.");
                    return Flux.empty();
                }))                .flatMap(leader ->
                        reportService.getSmallReportByRaceIdAndOffset(leader.getRaceId(), ONE_MONTH)
                                .map(report -> createEmailMessage(leader.getEmail(), report))
                )
                .doOnNext(mailSender::send)
                .doOnComplete(() -> log.info("Все письма успешно отправлены."))
                .doOnError(error -> log.error("Произошла ошибка при отправке писем: ", error))
                .subscribe();
    }

    private SimpleMailMessage createEmailMessage(String recipientEmail, String reportText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Ежемесячная статистика");
        message.setText(reportText);
        return message;
    }
}
