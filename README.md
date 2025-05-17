# Suprer Simple Scheduling Telegram Bot Writen in Java

## Using SQLite to store user data and Quartz to schedule for a reminder

Bot is added to Groupchat and users will be insert themselves to db using "/remindme@SoroushSelfServiceReminder_bot"
users also can remove themselves from db with "/dontremind@SoroushSelfServiceReminder_bot"
on a certain (premade) time, Bot will fetch all users from db and will mention them in the Groupchat to remind them of their task
