
package com.example.jopay;

import java.sql.*;
import java.time.LocalDateTime;

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

        return timeIn;
    }


    public boolean recordTimeIn(int employeeId, LocalDateTime timeNow) throws SQLException {
        Connection connection = null;
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            connection = connect.getConnection();

            // Check if a record for today exists
            String checkQuery = "SELECT time_IN, time_Out FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
            checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, employeeId);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Record exists for today
                Time existingTimeIn = rs.getTime("time_IN");
                Time existingTimeOut = rs.getTime("time_Out");



                if (existingTimeIn == null) {
                    // Employee was marked absent → update to Present
                    String updateQuery = "UPDATE time_log SET time_IN = ?, status = 'Present' " +
                            "WHERE employee_Id = ? AND log_date = CURDATE()";
                    updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setTime(1, Time.valueOf(timeNow.toLocalTime()));
                    updateStmt.setInt(2, employeeId);
                    int updated = updateStmt.executeUpdate();

                    return true;

                } else if (existingTimeOut != null) {
                    // Already completed shift (both time-in and time-out)
                    System.out.println("[TIME-IN] Already completed shift today");
                    return false;
                } else {
                    // Already timed in but not timed out yet
                    System.out.println("[TIME-IN] Already timed in, waiting for time-out");
                    return false;
                }

            } else {
                // No record for today → insert new
                String insertQuery = "INSERT INTO time_log (employee_Id, log_date, time_IN, status) " +
                        "VALUES (?, CURDATE(), ?, 'Present')";
                insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setInt(1, employeeId);
                insertStmt.setTime(2, Time.valueOf(timeNow.toLocalTime()));
                int inserted = insertStmt.executeUpdate();

                return true;
            }

        } finally {
            if (rs != null) rs.close();
            if (checkStmt != null) checkStmt.close();
            if (updateStmt != null) updateStmt.close();
            if (insertStmt != null) insertStmt.close();
            if (connection != null) connection.close();
        }
    }


    public boolean recordTimeOut(int employeeId, LocalDateTime timeNow, double totalHours, String attendanceStatus) throws SQLException {
        Connection connection = null;
        PreparedStatement verifyStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet verifyRs = null;

        try {
            connection = connect.getConnection();

            // Verify there's an active time-in
            String verifyQuery = "SELECT time_IN, time_Out FROM time_log " +
                    "WHERE employee_id = ? AND log_date = CURDATE()";
            verifyStmt = connection.prepareStatement(verifyQuery);
            verifyStmt.setInt(1, employeeId);
            verifyRs = verifyStmt.executeQuery();

            if (!verifyRs.next()) {
                System.out.println("[TIME-OUT] No record found for today");
                return false;
            }

            Time existingTimeIn = verifyRs.getTime("time_IN");
            Time existingTimeOut = verifyRs.getTime("time_Out");

            System.out.println("[TIME-OUT] Verify - TimeIn: " + existingTimeIn + ", TimeOut: " + existingTimeOut);

            if (existingTimeIn == null) {
                System.out.println("[TIME-OUT] No time-in found, cannot time-out");
                return false;
            }

            if (existingTimeOut != null) {
                System.out.println("[TIME-OUT] Already timed out");
                return false;
            }

            // Update the time-out record
            String updateQuery = "UPDATE time_log " +
                    "SET time_Out = ?, total_hours = ?, status = ? " +
                    "WHERE employee_Id = ? AND log_date = CURDATE() " +
                    "AND time_IN IS NOT NULL AND time_out IS NULL";
            updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setTime(1, Time.valueOf(timeNow.toLocalTime()));
            updateStmt.setDouble(2, totalHours);
            updateStmt.setString(3, attendanceStatus);
            updateStmt.setInt(4, employeeId);

            int rowsAffected = updateStmt.executeUpdate();
            System.out.println("[TIME-OUT] Update successful. Rows: " + rowsAffected +
                    ", Status: " + attendanceStatus + ", Hours: " + totalHours);

            return rowsAffected > 0;

        } finally {
            if (verifyRs != null) verifyRs.close();
            if (verifyStmt != null) verifyStmt.close();
            if (updateStmt != null) updateStmt.close();
            if (connection != null) connection.close();
        }
    }

    // Get current status
    public String getStatus(int employeeId) throws SQLException {
        Connection connection = connect.getConnection();
        String query = "SELECT status FROM time_log WHERE employee_Id = ? AND log_date = CURDATE()";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();
        String status = rs.next() ? rs.getString("status") : null;
        rs.close();
        stmt.close();
        connection.close();
        return status;
    }
}