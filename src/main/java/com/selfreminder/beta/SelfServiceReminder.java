package com.selfreminder.beta;
import com.selfreminder.beta.models.dbConnection;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SelfServiceReminder extends TelegramLongPollingBot {
    //default chat id #-1002330642857
    //need to make a dynamic chat id system for further development
    @Override
    public void onUpdateReceived (Update update){
        String input=update.getMessage().getText();
        String userID=update.getMessage().getFrom().getId().toString();
        String userName=update.getMessage().getFrom().getUserName();

        if(input.equals("/remindme@SoroushSelfServiceReminder_bot") &&
                !"private".equals(update.getMessage().getChat().getType())){
            if(!checkIfExists(userID)){
                addUser(userID,userName);
                responseToUser("شما به لیسته گرسنگان اضافه شدید",update);
            }
            else
                responseToUser("گفتم بس! شما در لیست گرسنگان حضور دارید",update);

        }
        else if(input.equals("/dontremind@SoroushSelfServiceReminder_bot") &&
                !"private".equals(update.getMessage().getChat().getType())){
            if(checkIfExists(userID)){
                deleteUser(userID);
                responseToUser("خارج شدنتان از لیست گرسنگان را تبریک می گویم",update);
            }
            else
                responseToUser("گفتم بس! شما در لیست گرسنگان حضور ندارید",update);
        }
    }

    @Override
    public String getBotUsername(){
        return "SoroushSelfServiceReminder_bot";
    }

    @Override
    public String getBotToken(){
        return "7679285942:AAEieLCYjqlOhetrPiTSs6DcXz6N6p1FCJo";
    }

    private void responseToUser(String responseText,Update update){
        SendMessage response=new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText(responseText);
        try {
            execute(response);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }


    private boolean checkIfExists(String target) {
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        if (connect != null) {
            String selectQuery = "SELECT userId FROM Users WHERE userId = ?";
            try (PreparedStatement statement = connect.prepareStatement(selectQuery)) {
                statement.setString(1, target);
                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        db.closeCon();
        return false;
    }


    private void addUser(String userId , String userFirstName){
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        if(connect!=null){
            String insertQuery="INSERT INTO Users VALUES(?,?)";
            try(PreparedStatement statement=connect.prepareStatement(insertQuery)){
                statement.setString(1,userId);
                statement.setString(2,userFirstName);
                statement.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        db.closeCon();
    }


    private void deleteUser(String userId){
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        if(connect!=null){
            String deleteQuery="DELETE FROM Users WHERE userId=?";
            try {
                PreparedStatement statement=connect.prepareStatement(deleteQuery);
                statement.setString(1,userId);
                statement.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        db.closeCon();
    }


    private HashMap<String,String> getUsers(){
        HashMap<String,String> userList=new HashMap<>();
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        if(connect!=null){
            String selectQuery="SELECT * FROM Users WHERE 1";
            try {
                Statement statement=connect.createStatement();
                ResultSet fetchedData= statement.executeQuery(selectQuery);
                while (fetchedData.next()){
                    userList.put(fetchedData.getString("userId")
                            ,fetchedData.getString("userName") );
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        db.closeCon();
        return userList;
    }


    public void mentionAll(String announcement){
        HashMap<String,String> users=getUsers();
        StringBuilder mentionMessage=new StringBuilder(announcement+'\n');
        for(Map.Entry<String,String> userList : users.entrySet())
            mentionMessage.append("["+userList.getKey()+"](tg://user?id=")
                    .append(userList.getValue()).append(")\n");
        SendMessage message=new SendMessage();
        //using the chat id manually, should fix this later on
        message.setChatId("-1002330642857");
        message.setText(mentionMessage.toString());
        message.setParseMode("MarkdownV2");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
