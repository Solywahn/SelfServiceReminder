package com.selfreminder.beta;
import com.selfreminder.beta.models.initializer;
import com.selfreminder.beta.scheduling.Quartz;
import org.quartz.SchedulerException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Launcher
{
    public static void main( String[] args )
    {
        initializer dbBuild=new initializer();
        try {
            Quartz scheduling=new Quartz();
            scheduling.schedule();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        try {
            TelegramBotsApi botsApi=new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new SelfServiceReminder());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
