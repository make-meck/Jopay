package com.example.jopay;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimelogModel {
    private final DatabaseConnector connect;


    public TimelogModel(){
        this.connect = new DatabaseConnector();
    }

    //hahanapin niya ung employeeID sa database
    public ResultSet getEmployeebyID(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT * FROM employee_info WHERE employee_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        return stmt.executeQuery();
    }

    //to check if the employee is active (nagawa niyang magtime-in but not time-out pa)
    public boolean hasTimedInToday(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT * FROM time_log WHERE employee_id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        connection.close();
        return exists;
    }
    public boolean hasTimedOutToday(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT * FROM time_log WHERE employee_id = ? AND log_date = CURDATE() AND time_out IS NOT NULL";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        connection.close();
        return exists;
    }
    public Time getTimeIn(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT time_in FROM time_log WHERE employee_id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        Time timeIn = null;
        if (rs.next()) {
            timeIn = rs.getTime("time_in");
        }
        rs.close();
        stmt.close();
        connection.close();
        return timeIn;
    }


    // records the TIME IN of the Employees

    public boolean recordTimeIn(int employeeId, LocalDateTime timeNow) throws SQLException{
        Connection connection = connect.getConnection();
        String insert = "INSERT INTO time_log (employee_id, log_date, time_IN) values (?,?,?)";
        PreparedStatement stmt1 = connection.prepareStatement(insert);
        stmt1.setInt(1, employeeId);
        stmt1.setDate(2, Date.valueOf(timeNow.toLocalDate()));
        stmt1.setTime(3,Time.valueOf(timeNow.toLocalTime()));
        stmt1.executeUpdate();
        stmt1.close();
        connection.close();
        return true;
    }

   public void recordTimeOut(int employeeId, LocalDateTime timeNow, double totalHours, String attendanceStatus) throws SQLException {
       Connection connection = connect.getConnection();
       String update = "UPDATE time_log SET time_out = ?, total_hours = ?, status = ? " +
               "WHERE employee_id = ? AND log_date = CURDATE() AND time_out IS NULL";
       PreparedStatement stmt = connection.prepareStatement(update);
       stmt.setTime(1, Time.valueOf(timeNow.toLocalTime()));
       stmt.setDouble(2, totalHours);
       stmt.setString(3, attendanceStatus);
       stmt.setInt(4, employeeId);
       stmt.executeUpdate();
       stmt.close();
       connection.close();
   }


    public String getStatus (int employeeId) throws SQLException {
            Connection connection = connect.getConnection();
            String query = "SELECT * status FROM time_log WHERE employee_Id =? AND log_date =CURDATE()";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, employeeId);
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs= statement.executeQuery();
            String status = rs.next() ? rs.getString("status"): "Present";
            rs.close();
            stmt.close();
            connection.close();
            return status;
    }


    public void autoMarkAbsences() {
        
        if (LocalTime.now().isBefore(LocalTime.of(23, 59))) {

        }

        String sql = """
        INSERT INTO time_log (employee_Id, log_date, status)
        SELECT e.employee_Id, CURDATE(), 'Absent'
        FROM employee_info e
        LEFT JOIN time_log t
            ON e.employee_Id = t.employee_Id 
            AND t.log_date = CURDATE()
        WHERE t.employee_Id IS NULL
        AND e.is_Active = 1
    """;

        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            int count = stmt.executeUpdate();
            System.out.println("AUTO ABSENT: " + count + " employees marked absent.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}



