package blps.duo.config;

import blps.duo.quartz.EmailJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(EmailJob.class)
                .withIdentity("emailJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity("emailTrigger")
                //CronScheduleBuilder.cronSchedule("0 * * * * ?")
                //CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 0, 0)
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 0, 0))
                .build();
    }
}
