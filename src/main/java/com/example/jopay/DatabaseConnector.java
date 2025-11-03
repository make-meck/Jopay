package com.example.jopay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:mysql://mysql-25123e52-tip-9280.h.aivencloud.com:15872/buhayinCo_db?sslMode=REQUIRED";
    private static final String USER= "avnadmin";
    private static final String PASSWORD = "AVNS_jAPKvAIP9tFpxAbeacP";// papalitan ung password if needed

    private static Connection connection;

   public static Connection getConnection() throws SQLException {
       if(connection == null || connection.isClosed()){
           connection= DriverManager.getConnection(URL, USER, PASSWORD);
       }
        return connection;
   }

  public void testConnection() {
       try (Connection con = getConnection()){
           System.out.println("Connected to Database" + getConnection().getCatalog());
       } catch (SQLException e){
           System.out.println("Connection failed" + e.getMessage());
           e.printStackTrace();
       }
   }

    public PreparedStatement prepareStatement(String query) throws SQLException {
    return getConnection().prepareStatement(query);
    }

    public void close() {
    try{
        if(connection != null && !connection.isClosed()){
            connection.close();
            System.out.println("The database is disconnected");
        }
    } catch (SQLException e){
        e.printStackTrace();
    }
    }
}
