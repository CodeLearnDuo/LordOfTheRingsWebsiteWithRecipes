package blps.duo.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailJob implements Job {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        sendEmail();
    }

    public void sendEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("recipient@example.com");
        message.setSubject("Monthly Statistics");
        message.setText("Here are your monthly statistics...");
        mailSender.send(message);
    }
}
