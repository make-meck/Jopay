package com.example.jopay;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class timeController {
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private TextField timeInEmployeeID;
    @FXML private Label loginInfo;
    @FXML private Label employeeInfoName;
    @FXML private Label employeeInfoId;
    @FXML private Label employeeInfoDept;
    @FXML private Label statusLabel;
    @FXML private Pane employeeInfoPane;
    @FXML private TextField timeOutEmployeeID;
    @FXML private AnchorPane timelogs;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EE, MMMM dd, yyyy");
    private final TimelogModel timelog = new TimelogModel();

    public enum AttendanceStatus {
        Present, Undertime, Overtime
    }

    @FXML
    public void initialize() {
        System.out.println("=== INITIALIZING TIME CONTROLLER ===");

        try {
            timelog.markAbsentForNonClockedInEmployees();
            System.out.println("✓ Absent marking completed");
        } catch (SQLException e) {
            System.err.println("✗ Error marking absences: " + e.getMessage());
            e.printStackTrace();
        }

        updateDateTime();

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        timeInEmployeeID.setOnAction(event -> recordTimeIn());
        timeOutEmployeeID.setOnAction(actionEvent -> recordTimeOut());
    }

    private void recordTimeIn() {
        System.out.println("\n=== TIME IN REQUEST ===");
        String employeeIdText = timeInEmployeeID.getText().trim();

        if (employeeIdText.isEmpty()) {
            statusLabel.setText("Please enter your Employee ID");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int employeeId = Integer.parseInt(employeeIdText);
            System.out.println("Employee ID: " + employeeId);

            ResultSet rs = timelog.getEmployeebyID(employeeId);

            if (rs.next()) {
                // Display employee info
                String firstName = rs.getString("employee_FirstName");
                String lastName = rs.getString("employee_LastName");
                String department = rs.getString("employee_Department");

                employeeInfoName.setText(firstName + " " + lastName);
                employeeInfoId.setText(String.valueOf(employeeId));
                employeeInfoDept.setText(department);
                employeeInfoPane.setVisible(true);

                LocalDateTime now = LocalDateTime.now();
                boolean success = timelog.recordTimeIn(employeeId, now);

                if (success) {
                    loginInfo.setText("LOG-IN TIME: " + now.format(timeFormatter));
                    statusLabel.setText("✓ Time-in recorded successfully! Status: Present");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    System.out.println("✓ Time-in successful");
                } else {
                    // Check if already timed out
                    if (timelog.hasTimedOutToday(employeeId)) {
                        statusLabel.setText("⚠ You have already completed your shift for today!");
                        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        System.out.println("✗ Already completed shift");
                    } else {
                        statusLabel.setText("⚠ You are already timed in! Please time out first.");
                        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        System.out.println("✗ Already timed in");
                    }
                }

                rs.close();

            } else {
                ShowEmployeeNotFound();
                System.out.println("✗ Employee not found");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            System.err.println("✗ SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Employee ID Format");
            statusLabel.setStyle("-fx-text-fill: red;");
            System.err.println("✗ Invalid ID format");
        } finally {
            timeInEmployeeID.clear();
        }
    }

    private void recordTimeOut() {
        System.out.println("\n=== TIME OUT REQUEST ===");
        String employeeIdText = timeOutEmployeeID.getText().trim();

        if (employeeIdText.isEmpty()) {
            statusLabel.setText("Please enter your Employee ID");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int employeeId = Integer.parseInt(employeeIdText);
            System.out.println("Employee ID: " + employeeId);

            ResultSet rs = timelog.getEmployeebyID(employeeId);

            if (rs.next()) {
                String firstName = rs.getString("employee_FirstName");
                String lastName = rs.getString("employee_LastName");
                String department = rs.getString("employee_Department");

                employeeInfoName.setText(firstName + " " + lastName);
                employeeInfoId.setText(String.valueOf(employeeId));
                employeeInfoDept.setText(department);
                employeeInfoPane.setVisible(true);

                rs.close();

                // Check conditions
                boolean hasActiveTimeIn = timelog.hasActiveTimeInToday(employeeId);
                boolean hasTimedOut = timelog.hasTimedOutToday(employeeId);
                boolean hasAnyTimeIn = timelog.hasAnyTimeInToday(employeeId);

                System.out.println("Has active time-in: " + hasActiveTimeIn);
                System.out.println("Has timed out: " + hasTimedOut);
                System.out.println("Has any time-in: " + hasAnyTimeIn);

                if (!hasAnyTimeIn) {
                    statusLabel.setText("⚠ You haven't timed in today yet!");
                    statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    System.out.println("✗ No time-in record");
                    return;
                }

                if (hasTimedOut) {
                    statusLabel.setText("⚠ You have already timed out today!");
                    statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    System.out.println("✗ Already timed out");
                    return;
                }

                if (!hasActiveTimeIn) {
                    statusLabel.setText("⚠ No active time-in session found!");
                    statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    System.out.println("✗ No active session");
                    return;
                }

                LocalDateTime now = LocalDateTime.now();

                // Get time-in
                Time timeIn = timelog.getTimeIn(employeeId);
                if (timeIn == null) {
                    statusLabel.setText("⚠ No time-in record found!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    System.out.println("✗ Time-in is null");
                    return;
                }

                // Calculate total hours worked
                LocalTime in = timeIn.toLocalTime();
                LocalTime out = now.toLocalTime();
                long minutesWorked = java.time.Duration.between(in, out).toMinutes();
                double totalHours = minutesWorked / 60.0;

                System.out.println("Time In: " + in);
                System.out.println("Time Out: " + out);
                System.out.println("Minutes worked: " + minutesWorked);
                System.out.println("Total hours: " + totalHours);

                // Determine attendance status
                AttendanceStatus attendanceStatus;
                if (totalHours >= 8.0) {
                    if (totalHours > 8.0) {
                        attendanceStatus = AttendanceStatus.Overtime;
                    } else {
                        attendanceStatus = AttendanceStatus.Present;
                    }
                } else {
                    attendanceStatus = AttendanceStatus.Undertime;
                }

                System.out.println("Attendance Status: " + attendanceStatus.name());

                // Record time-out
                boolean success = timelog.recordTimeOut(employeeId, now, totalHours, attendanceStatus.name());

                if (success) {
                    loginInfo.setText("LOG-OUT TIME: " + now.format(timeFormatter));
                    statusLabel.setText(String.format(
                            "✓ Time-out recorded! Total Hours: %.2fh | Status: %s",
                            totalHours,
                            attendanceStatus.name()
                    ));
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    System.out.println("✓ Time-out successful");
                } else {
                    statusLabel.setText("✗ Failed to record time-out. Please try again.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    System.out.println("✗ Time-out failed");
                }

            } else {
                ShowEmployeeNotFound();
                System.out.println("✗ Employee not found");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            System.err.println("✗ SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Employee ID format");
            statusLabel.setStyle("-fx-text-fill: red;");
            System.err.println("✗ Invalid ID format");
        } finally {
            timeOutEmployeeID.clear();
        }
    }

    private void ShowEmployeeNotFound() {
        statusLabel.setText("❌ Employee Does Not Exist");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        loginInfo.setText("");
        employeeInfoName.setText("");
        employeeInfoId.setText("");
        employeeInfoDept.setText("");
        employeeInfoPane.setVisible(false);
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        timeLabel.setText(now.format(timeFormatter));
        dateLabel.setText(now.format(dateFormatter));
    }
}