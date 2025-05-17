package com.selfreminder.beta.scheduling;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.newJob;

import com.selfreminder.beta.SelfServiceReminder;


public class Quartz {
    public void schedule() throws SchedulerException{
        Scheduler scheduler=StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        JobDetail mondayJob=newJob(hourOfReckoning.class)
                .withIdentity("firstJob")
                .build();
        JobDetail wednesdayJob=newJob(hourOfTwilight.class)
                .withIdentity("secondJob")
                .build();
        Trigger mondayTrigger=TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity("Monday Mention")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 18 ? * MON *"))
                .build();
        Trigger wednesdayTrigger=TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity("Wednesday Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 18 ? * WED *"))
                .build();


        scheduler.scheduleJob(mondayJob,mondayTrigger);
        scheduler.scheduleJob(wednesdayJob,wednesdayTrigger);
    }
    public static class hourOfReckoning implements Job{
        @Override
        public void execute(JobExecutionContext context){
            SelfServiceReminder sr=new SelfServiceReminder();
            sr.mentionAll("گرسنگان سلف باز شده ها فراموشتون نشه");
        }
    }
    public static class hourOfTwilight implements Job{
        @Override
        public void execute(JobExecutionContext context){
            SelfServiceReminder sr=new SelfServiceReminder();
            sr.mentionAll("گرسنگان آخرین مهلت رزرو سلف امشبه ها بجنبید");
        }
    }
}
