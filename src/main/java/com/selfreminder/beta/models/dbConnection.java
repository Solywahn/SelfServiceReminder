package com.selfreminder.beta.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnection {
    private Connection connect;
    final String dbPath="database/selfservice_db.sqlite";
    public dbConnection(){
        try {
            connect= DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Connection getConnect() {
        return connect;
    }
    public void closeCon(){
        try {
            if(connect!=null){
                connect.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
