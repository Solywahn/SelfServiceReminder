package com.selfreminder.beta;
import com.selfreminder.beta.models.dbConnection;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelfServiceReminder extends TelegramLongPollingBot {
    //default chat id #-1002330642857
    //need to make a dynamic Group chat id system for further development
    @Override
    public void onUpdateReceived (Update update){
        if(update.hasCallbackQuery()){
            CallbackQuery cb=update.getCallbackQuery();
            if(cb.getData().equals("turn_RapidMode")){
                String uId=cb.getFrom().getId().toString();
                AnswerCallbackQuery alert=new AnswerCallbackQuery();
                alert.setCallbackQueryId(cb.getId());
                if(!checkIfExists(uId))
                    alert.setText("این گزینه فقط برای اعضای گرسنه قابل استفاده است");
                else{
                    if(turnRapidMode(uId)==1)
                        alert.setText("یادآور مکرر فعال شد (باید بات رو استارت کرده باشی)");
                    else
                        alert.setText("یادآور مکرر غیرفعال شد");
                }
                alert.setShowAlert(true);
                try {
                    execute(alert);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
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
            else if(input.equals("/start")){
                dbConnection db=new dbConnection();
                Connection connect=db.getConnect();
                if(connect!=null){
                    String selectQuery="SELECT hasStarted FROM Users WHERE userId=?";
                    try(PreparedStatement selectStatement=connect.prepareStatement(selectQuery)){
                        selectStatement.setString(1,userID);
                        ResultSet rS=selectStatement.executeQuery();
                        if(rS.next()){
                            String updateQuery="UPDATE Users SET hasStarted=1 WHERE userId=?";
                            PreparedStatement updateStatement=connect.prepareStatement(updateQuery);
                            updateStatement.setString(1,userID);
                            updateStatement.executeUpdate();
                        }
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
                db.closeCon();
                responseToUser("خوش اومدی گرسنه! حالا توی گروه از من بخواه تا توی لیست گرسنگان اضافت کنم تا دیگه سلف یادت نره",update);
            }
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
        if(response.getText().contains("اضافه شدید")){
//            InlineKeyboardMarkup keyboardMarkup=new InlineKeyboardMarkup();
//            InlineKeyboardButton btnRapidMode=new InlineKeyboardButton();
//            btnRapidMode.setText("Turn On/Off Rapid Reminder");
//            btnRapidMode.setCallbackData("turn_RapidMode");
//
//            List<InlineKeyboardButton> row=new ArrayList<>();
//            row.add(btnRapidMode);
//
//            List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
//            keyboard.add(row);
//
//            keyboardMarkup.setKeyboard(keyboard);
            InlineKeyboardMarkup markup=addKey("turn_RapidMode","Turn On/Off Rapid Reminder");
            response.setReplyMarkup(markup);
        }


        response.setReplyToMessageId(update.getMessage().getMessageId());
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
            String insertQuery="INSERT INTO Users VALUES(?,?,0,0,0)";
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


    private HashMap<String,String> getUsers(boolean isRapid){
        HashMap<String,String> userList=new HashMap<>();
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        if(connect!=null){
            String selectQuery="SELECT * FROM Users";
            if(isRapid)
                selectQuery+=" WHERE rapidMode=1 AND hasStarted=1";
            try {
                Statement statement=connect.createStatement();
                ResultSet fetchedData= statement.executeQuery(selectQuery);
                if(!isRapid)
                    while (fetchedData.next()){
                        userList.put(fetchedData.getString("userId")
                                ,fetchedData.getString("userName") );
                    }
                else
                    while (fetchedData.next()){
                        userList.put(fetchedData.getString("userId")
                                ,fetchedData.getString("lastMessage") );
                    }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        db.closeCon();
        return userList;
    }


    public void mentionAll(String announcement){
        HashMap<String,String> users=getUsers(false);
        if(users.isEmpty())
            return;
        StringBuilder mentionMessage=new StringBuilder(announcement+'\n');
        for(Map.Entry<String,String> userList : users.entrySet()){
            mentionMessage.append(String.format("<a href=\"tg://user?id=%s\">%s</a>\n",
                    userList.getKey(), userList.getValue()));
        }
        SendMessage message=new SendMessage();
        //using the chat id manually, should fix this later on
        //message.setChatId("-1002330642857");
        //test
        message.setChatId("-1002352471621");
        //
        message.setText(mentionMessage.toString());
        message.setParseMode("HTML");
        try {
            Message sent = execute(message);
            PinChatMessage mentionPin=new PinChatMessage();
            mentionPin.setChatId(message.getChatId());
            mentionPin.setMessageId(sent.getMessageId());
            execute(mentionPin);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        //

    }


    private int turnRapidMode(String target){
        dbConnection db=new dbConnection();
        Connection connect=db.getConnect();
        int rapidMode=0;
        if(connect!=null){
            String selectQuery="SELECT rapidMode FROM Users WHERE userId = ?";
            try(PreparedStatement selectStatement=connect.prepareStatement(selectQuery)){
                selectStatement.setString(1,target);
                ResultSet getUserInfo=selectStatement.executeQuery();
                rapidMode=getUserInfo.getInt("rapidMode");
                if(rapidMode==1)
                    rapidMode=0;
                else
                    rapidMode=1;
                String updateQuery="UPDATE Users SET rapidMode=? WHERE userId =?";
                PreparedStatement updateStatement=connect.prepareStatement(updateQuery);
                updateStatement.setInt(1,rapidMode);
                updateStatement.setString(2,target);
                updateStatement.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }

        }
        db.closeCon();
        return rapidMode;
    }

    private InlineKeyboardMarkup addKey(String callBackData,String Text){
        InlineKeyboardMarkup keyboardMarkup=new InlineKeyboardMarkup();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText(Text);
        button.setCallbackData(callBackData);

        List<InlineKeyboardButton> row=new ArrayList<>();
        row.add(button);

        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public void rapidRemind(){
        HashMap<String,String> users=getUsers(true);
        if(users.isEmpty())
            return;
        SendMessage pv=new SendMessage();
        pv.setText("پاشو بدو سلفتو رزرو کن گرسنه");
        InlineKeyboardMarkup markup=addKey("turn_RapidMode","من گفتم بس!");
        pv.setReplyMarkup(markup);
        DeleteMessage previousMessage=new DeleteMessage();
        for(Map.Entry<String,String> ChatIdList:users.entrySet()){
            pv.setChatId(ChatIdList.getKey());
            previousMessage.setMessageId(Integer.parseInt(ChatIdList.getValue()));
            previousMessage.setChatId(ChatIdList.getKey());
            try {
                if(previousMessage.getMessageId()!=0)
                    execute(previousMessage);
                Message messageSent=execute(pv);
                dbConnection db=new dbConnection();
                Connection connect=db.getConnect();
                if(connect!=null){
                    String updateQuery="UPDATE Users SET lastMessage=? WHERE userId=?";
                    try(PreparedStatement statement=connect.prepareStatement(updateQuery)){
                        statement.setString(1,messageSent.getMessageId().toString());
                        statement.setString(2,ChatIdList.getKey());
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                db.closeCon();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
