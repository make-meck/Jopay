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

    // Find employee by ID
    public ResultSet getEmployeebyID(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT * FROM employee_info WHERE employee_Id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        return stmt.executeQuery();
    }

    // Check if employee has ANY time-in record today (regardless of time-out)
    public boolean hasAnyTimeInToday(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT time_IN FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();

        boolean hasTimeIn = false;
        if (rs.next()) {
            Time timeIn = rs.getTime("time_IN");
            hasTimeIn = (timeIn != null);
        }

        rs.close();
        stmt.close();
        connection.close();

        System.out.println("Employee " + employeeId + " has time-in today: " + hasTimeIn);
        return hasTimeIn;
    }

    // Check if employee currently has active time-in (timed in but NOT timed out)
    public boolean hasActiveTimeInToday(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT time_IN, time_out FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();

        boolean isActive = false;
        if (rs.next()) {
            Time timeIn = rs.getTime("time_IN");
            Time timeOut = rs.getTime("time_Out");
            // Active means: has time_in AND no time_out
            isActive = (timeIn != null && timeOut == null);
        }

        rs.close();
        stmt.close();
        connection.close();

        System.out.println("Employee " + employeeId + " has active time-in: " + isActive);
        return isActive;
    }

    // Check if employee has already timed out today
    public boolean hasTimedOutToday(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT time_Out FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();

        boolean hasTimedOut = false;
        if (rs.next()) {
            Time timeOut = rs.getTime("time_Out");
            hasTimedOut = (timeOut != null);
        }

        rs.close();
        stmt.close();
        connection.close();

        System.out.println("Employee " + employeeId + " has timed out today: " + hasTimedOut);
        return hasTimedOut;
    }

    // Get time-in for today
    public Time getTimeIn(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT time_IN FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        Time timeIn = null;
        if (rs.next()) {
            timeIn = rs.getTime("time_IN");
        }
        rs.close();
        stmt.close();
        connection.close();

        System.out.println("Employee " + employeeId + " time-in: " + timeIn);
        return timeIn;
    }

    // Record TIME IN
    public boolean recordTimeIn(int employeeId, LocalDateTime timeNow) throws SQLException {
        Connection connection = connect.getConnection();

        // Check if a record for today exists
        String checkQuery = "SELECT time_IN, time_Out FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
        checkStmt.setInt(1, employeeId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // Record exists for today
            Time existingTimeIn = rs.getTime("time_IN");
            Time existingTimeOut = rs.getTime("time_Out");

            System.out.println("Existing record found - TimeIn: " + existingTimeIn + ", TimeOut: " + existingTimeOut);

            if (existingTimeIn == null) {
                // Employee was marked absent → update to Present
                String updateQuery = "UPDATE time_log SET time_IN = ?, status = 'Present' WHERE employee_Id = ? AND log_date = CURDATE()";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setTime(1, Time.valueOf(timeNow.toLocalTime()));
                updateStmt.setInt(2, employeeId);
                int updated = updateStmt.executeUpdate();
                updateStmt.close();

                System.out.println("Updated absent record to present. Rows affected: " + updated);

                rs.close();
                checkStmt.close();
                connection.close();
                return true;

            } else if (existingTimeOut != null) {
                // Already completed time-in AND time-out for today
                System.out.println("Employee already completed shift today");
                rs.close();
                checkStmt.close();
                connection.close();
                return false;
            } else {
                // Already timed in but not timed out yet
                System.out.println("Employee already timed in, waiting for time-out");
                rs.close();
                checkStmt.close();
                connection.close();
                return false;
            }

        } else {
            // No record for today → insert new
            String insertQuery = "INSERT INTO time_log (employee_Id, log_date, time_IN, status) VALUES (?, CURDATE(), ?, 'Present')";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setInt(1, employeeId);
            insertStmt.setTime(2, Time.valueOf(timeNow.toLocalTime()));
            int inserted = insertStmt.executeUpdate();
            insertStmt.close();

            System.out.println("New time-in record created. Rows affected: " + inserted);
        }

        rs.close();
        checkStmt.close();
        connection.close();
        return true;
    }

    // Record TIME OUT
    public boolean recordTimeOut(int employeeId, LocalDateTime timeNow, double totalHours, String attendanceStatus) throws SQLException {
        Connection connection = connect.getConnection();

        // First verify there's an active time-in
        String verifyQuery = "SELECT time_IN, time_Out FROM time_log WHERE employee_id = ? AND log_date = CURDATE()";
        PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
        verifyStmt.setInt(1, employeeId);
        ResultSet verifyRs = verifyStmt.executeQuery();

        if (!verifyRs.next()) {
            System.out.println("No record found for today");
            verifyRs.close();
            verifyStmt.close();
            connection.close();
            return false;
        }

        Time existingTimeIn = verifyRs.getTime("time_IN");
        Time existingTimeOut = verifyRs.getTime("time_Out");

        System.out.println("Verify - TimeIn: " + existingTimeIn + ", TimeOut: " + existingTimeOut);

        if (existingTimeIn == null) {
            System.out.println("No time-in found, cannot time-out");
            verifyRs.close();
            verifyStmt.close();
            connection.close();
            return false;
        }

        if (existingTimeOut != null) {
            System.out.println("Already timed out");
            verifyRs.close();
            verifyStmt.close();
            connection.close();
            return false;
        }

        verifyRs.close();
        verifyStmt.close();

        // Now update the time-out
        String updateQuery = "UPDATE time_log SET time_Out = ?, total_hours = ?, status = ? " +
                "WHERE employee_Id = ? AND log_date = CURDATE() AND time_IN IS NOT NULL AND time_out IS NULL";
        PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
        updateStmt.setTime(1, Time.valueOf(timeNow.toLocalTime()));
        updateStmt.setDouble(2, totalHours);
        updateStmt.setString(3, attendanceStatus);
        updateStmt.setInt(4, employeeId);

        int rowsAffected = updateStmt.executeUpdate();
        System.out.println("Time-out update - Rows affected: " + rowsAffected);

        updateStmt.close();
        connection.close();

        return rowsAffected > 0;
    }

    // Get current status
    public String getStatus(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT status FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        String status = rs.next() ? rs.getString("status") : "Present";
        rs.close();
        stmt.close();
        connection.close();
        return status;
    }

    // Mark absent for employees who haven't clocked in
    public void markAbsentForNonClockedInEmployees() throws SQLException {
        Connection connection = connect.getConnection();

        // Get all active employees
        String employeeQuery = "SELECT employee_Id FROM employee_info WHERE is_Active = 1 OR employement_Status = 'Probationary' OR employee_status = 'Regular'";
        PreparedStatement empStmt = connection.prepareStatement(employeeQuery);
        ResultSet empRs = empStmt.executeQuery();

        LocalDate today = LocalDate.now();
        int markedAbsent = 0;

        while (empRs.next()) {
            int employeeId = empRs.getInt("employee_Id");

            // Check if employee has any record for today
            String timeLogQuery = "SELECT time_IN FROM time_log WHERE employee_Id = ? AND log_date = ?";
            PreparedStatement timeStmt = connection.prepareStatement(timeLogQuery);
            timeStmt.setInt(1, employeeId);
            timeStmt.setDate(2, java.sql.Date.valueOf(today));

            ResultSet timeRs = timeStmt.executeQuery();
            boolean hasTimedIn = false;

            if (timeRs.next()) {
                Time timeIn = timeRs.getTime("time_IN");
                if (timeIn != null) {
                    hasTimedIn = true;
                }
            }

            timeRs.close();
            timeStmt.close();

            // Mark absent if not timed in
            if (!hasTimedIn) {
                String insertAbsentQuery = "INSERT INTO time_log (employee_Id, log_date, time_IN, time_Out, status) " +
                        "VALUES (?, ?, NULL, NULL, 'Absent') " +
                        "ON DUPLICATE KEY UPDATE status = IF(time_IN IS NULL, 'Absent', status)";
                PreparedStatement absentStmt = connection.prepareStatement(insertAbsentQuery);
                absentStmt.setInt(1, employeeId);
                absentStmt.setDate(2, java.sql.Date.valueOf(today));
                absentStmt.executeUpdate();
                absentStmt.close();
                markedAbsent++;
            }
        }

        System.out.println("Marked " + markedAbsent + " employees as absent");
        empRs.close();
        empStmt.close();
        connection.close();
    }
}