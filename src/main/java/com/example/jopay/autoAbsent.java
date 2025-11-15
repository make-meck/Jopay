package com.example.jopay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class autoAbsent {
    private DatabaseConnector connect; // your DB connection class

    public void AutoAbsent() {
        connect = new DatabaseConnector();
    }

    // 1. Get employees who haven't timed in today
    public List<Integer> getEmployeesNotTimedIn() throws SQLException {
        List<Integer> absentEmployees = new ArrayList<>();
        Connection connection = connect.getConnection();

        String query = "SELECT employee_id FROM employee_info " +
                "WHERE employee_Id NOT IN (" +
                "SELECT employee_Id FROM time_log WHERE log_date = CURDATE()" +
                ")";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            absentEmployees.add(rs.getInt("employee_Id"));
        }

        rs.close();
        stmt.close();
        connection.close();

        return absentEmployees;
    }

    // 2. Mark employees as absent
    public void markAbsent(List<Integer> employees) throws SQLException {
        if (employees.isEmpty()) return;

        Connection connection = connect.getConnection();
        String insertQuery = "INSERT INTO time_log (employee_Id, log_date, status) VALUES (?, CURDATE(), 'Absent')";
        PreparedStatement stmt = connection.prepareStatement(insertQuery);

        for (int id : employees) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        stmt.close();
        connection.close();
    }

    // 3. Method to run the auto-absent process
    public void runAutoAbsent() {
        try {
            List<Integer> absentEmployees = getEmployeesNotTimedIn();
            markAbsent(absentEmployees);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void scheduleDailyCheck() {
        Runnable task = this::runAutoAbsent;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(0).withMinute(30).withSecond(59);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }


}
