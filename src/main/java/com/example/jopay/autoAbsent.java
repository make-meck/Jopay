package com.example.jopay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class autoAbsent {
    private DatabaseConnector connect;

    public autoAbsent() {
        connect = new DatabaseConnector();
    }


    public void markAbsentEmployees() throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connect.getConnection();


            String query = "INSERT INTO time_log (employee_Id, log_date, time_IN, time_Out, status) " +
                    "SELECT ei.employee_Id, CURDATE(), NULL, NULL, 'Absent' " +
                    "FROM employee_info ei " +
                    "WHERE (ei.is_Active = 1 OR ei.employement_Status = 'Probationary' OR ei.employee_status = 'Regular') " +
                    "AND ei.employee_Id NOT IN ( " +
                    "    SELECT tl.employee_Id FROM time_log tl " +
                    "    WHERE tl.log_date = CURDATE() AND tl.time_IN IS NOT NULL " +
                    ") " +
                    "ON DUPLICATE KEY UPDATE " +
                    "status = IF(time_IN IS NULL, 'Absent', status)";

            stmt = connection.prepareStatement(query);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("[AUTO-ABSENT] Marked " + rowsAffected + " employees as absent at " +
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        } finally {
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
        }
    }

    public void runAutoAbsent() {
        try {
            markAbsentEmployees();
        } catch (SQLException e) {
            System.err.println("[AUTO-ABSENT] Error marking absences: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void scheduleDailyCheck(int hour, int minute) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            System.out.println("[AUTO-ABSENT] Running scheduled absence check...");
            runAutoAbsent();
        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        System.out.println("[AUTO-ABSENT] Scheduled to run at " +
                nextRun.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }


    public void scheduleDailyCheck() {
        scheduleDailyCheck(17, 30);
    }


    public void markAbsentForDate(java.sql.Date date) throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = connect.getConnection();

            String query = "INSERT INTO time_log (employee_Id, log_date, time_IN, time_Out, status) " +
                    "SELECT ei.employee_Id, ?, NULL, NULL, 'Absent' " +
                    "FROM employee_info ei " +
                    "WHERE (ei.is_Active = 1 OR ei.employement_Status = 'Probationary' OR ei.employee_status = 'Regular') " +
                    "AND ei.employee_Id NOT IN ( " +
                    "    SELECT tl.employee_Id FROM time_log tl " +
                    "    WHERE tl.log_date = ? AND tl.time_IN IS NOT NULL " +
                    ") " +
                    "ON DUPLICATE KEY UPDATE " +
                    "status = IF(time_IN IS NULL, 'Absent', status)";

            stmt = connection.prepareStatement(query);
            stmt.setDate(1, date);
            stmt.setDate(2, date);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("[AUTO-ABSENT] Marked " + rowsAffected + " employees as absent for " + date);

        } finally {
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
        }
    }
}