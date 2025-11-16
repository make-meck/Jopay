
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
    private final autoAbsent autoAbsentService = new autoAbsent();

    public enum AttendanceStatus {
        Present, Undertime, Overtime
    }

    @FXML
    public void initialize() {


        // Mark employees as absent on initialization (for employees who haven't shown up)
        try {
            autoAbsentService.runAutoAbsent();
            System.out.println("Initial absent marking completed");
        } catch (Exception e) {
            System.err.println(" Error marking absences: " + e.getMessage());
            e.printStackTrace();
        }

        // Schedule daily auto-absent check at 12:30 AM
        autoAbsentService.scheduleDailyCheck();

        // Start the clock
        updateDateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Set up event handlers
        timeInEmployeeID.setOnAction(event -> recordTimeIn());
        timeOutEmployeeID.setOnAction(actionEvent -> recordTimeOut());
    }

    private void recordTimeIn() {
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
                    statusLabel.setText("Time-in recorded successfully! Status: Present");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    // Check why it failed
                    if (timelog.hasTimedOutToday(employeeId)) {
                        statusLabel.setText("You have already completed your shift for today!");
                        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");

                    } else {
                        statusLabel.setText(" You are already timed in! Please time out first.");
                        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");

                    }
                }
                rs.close();

            } else {
                showEmployeeNotFound();
                System.out.println("Employee not found");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Employee ID Format");
            statusLabel.setStyle("-fx-text-fill: red;");
        } finally {
            timeInEmployeeID.clear();
        }
    }

    private void recordTimeOut() {
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
                    statusLabel.setText("You haven't timed in today yet!");

                    return;
                }

                if (hasTimedOut) {
                    statusLabel.setText(" You have already timed out today!");
                    return;
                }

                if (!hasActiveTimeIn) {
                    statusLabel.setText("No active time-in session found!");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    return;
                }

                LocalDateTime now = LocalDateTime.now();

                // Get time-in
                Time timeIn = timelog.getTimeIn(employeeId);
                if (timeIn == null) {
                    statusLabel.setText("No time-in record found!");
                    statusLabel.setStyle("-fx-text-fill: red;");
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

                // Determine attendance status based on hours worked
                AttendanceStatus attendanceStatus;
                if (totalHours >= 8.0) {
                    attendanceStatus = (totalHours > 8.0) ? AttendanceStatus.Overtime : AttendanceStatus.Present;
                } else {
                    attendanceStatus = AttendanceStatus.Undertime;
                }

                System.out.println("Attendance Status: " + attendanceStatus.name());

                // Record time-out with calculated status
                boolean success = timelog.recordTimeOut(employeeId, now, totalHours, attendanceStatus.name());

                if (success) {
                    loginInfo.setText("LOG-OUT TIME: " + now.format(timeFormatter));
                    statusLabel.setText(String.format(
                            "Time-out recorded! Total Hours: %.2fh | Status: %s",
                            totalHours,
                            attendanceStatus.name()
                    ));
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    statusLabel.setText("Failed to record time-out. Please try again.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

                }

            } else {
                showEmployeeNotFound();
                System.out.println("Employee not found");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Employee ID format");
            statusLabel.setStyle("-fx-text-fill: red;");
        } finally {
            timeOutEmployeeID.clear();
        }
    }

    private void showEmployeeNotFound() {
        statusLabel.setText("Employee Does Not Exist");
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