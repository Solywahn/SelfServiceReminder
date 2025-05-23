package com.selfreminder.beta.models;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.sql.Statement;

public class initializer {
    final String dbPath="database/selfservice_db.sqlite";
    public initializer(){
        File dataBase=new File(dbPath);
        if(!dataBase.exists()){
            try {
                dataBase.createNewFile();
                try {
                    Connection tempConnection=DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                    Statement createTable=tempConnection.createStatement();
                    String tableQuery=
                            "CREATE TABLE IF NOT EXISTS Users (userId TEXT PRIMARY KEY,userName TEXT,rapidMode INTEGER DEFAULT 0,hasStarted INTEGER DEFAULT 0,lastMessage TEXT)";
                    createTable.executeUpdate(tableQuery);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
