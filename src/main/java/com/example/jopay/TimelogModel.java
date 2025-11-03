package com.example.jopay;

import java.sql.*;
import java.time.LocalDateTime;

public class TimelogModel {
    private final DatabaseConnector connect;

    public TimelogModel(){
        this.connect = new DatabaseConnector();
    }

    public ResultSet getEmployeebyID(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT * FROM employee_info WHERE employee_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        return stmt.executeQuery();
    }

    public void recordTimeIn(int employeeId, LocalDateTime timeNow) throws SQLException{
        Connection connection = connect.getConnection();
        String insert = "INSERT INTO time_log (employee_id, log_date, time_IN) values (?,?,?)";
        PreparedStatement stmt1 = connection.prepareStatement(insert);
        stmt1.setInt(1, employeeId);
        stmt1.setDate(2, Date.valueOf(timeNow.toLocalDate()));
        stmt1.setTime(3,Time.valueOf(timeNow.toLocalTime()));
        stmt1.executeUpdate();
        stmt1.close();
        connection.close();
    }
}
