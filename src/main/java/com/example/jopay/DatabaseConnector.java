package com.example.jopay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:mysql://mysql-25123e52-tip-9280.h.aivencloud.com:15872/buhayinCo_db?sslMode=REQUIRED";
    private static final String USER= "avnadmin";
    private static final String PASSWORD = "AVNS_jAPKvAIP9tFpxAbeacP";

   public static Connection getConnection() throws SQLException {
       return DriverManager.getConnection(URL, USER, PASSWORD);
   }

  public void testConnection() {
       try (Connection con = getConnection()){
           System.out.println("Connected to Database" + getConnection().getCatalog());
       } catch (SQLException e){
           System.out.println("Connection failed" + e.getMessage());
           e.printStackTrace();
       }
   }
}
