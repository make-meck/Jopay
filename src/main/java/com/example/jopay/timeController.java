package com.example.jopay;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage. Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;



public class timeController {
 @FXML
   private Label dateLabel;

 @FXML
    private Label timeLabel;

 @FXML
    private TextField timeInEmployeeID;
 @FXML
 private Label loginInfo;
 @FXML
    private Label employeeInfoName;
 @FXML
    private Label employeeInfoId;
 @FXML
    private Label employeeInfoDept;
 @FXML
    private Label statusLabel;
 @FXML
 private Pane employeeInfoPane;
 @FXML
 private TextField timeOutEmployeeID;
@FXML
private AnchorPane timelogs;

 private final DateTimeFormatter timeFormatter= DateTimeFormatter.ofPattern("HH:mm:ss");
 private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EE, MMMM dd, yyyy");

  private final TimelogModel timelog= new TimelogModel();
  public enum AttendanceStatus{
      Present, Undertime, Overtime
  }

 @FXML
    public void initialize(){
      timelog.autoMarkAbsences();

     updateDateTime();

     Timeline clock= new Timeline( new KeyFrame(Duration.seconds(1), event -> updateDateTime())
     );
     clock.setCycleCount(Timeline.INDEFINITE);
     clock.play();

     timeInEmployeeID.setOnAction(event -> recordTimeIn());
     timeOutEmployeeID.setOnAction(actionEvent -> recordTimeOut());
 }

    private void recordTimeIn(){
        String employeeIdText= timeInEmployeeID.getText().trim();
        if(employeeIdText.isEmpty()){
            statusLabel.setText("Please enter you Employee ID");
            return;
        }
        try{
            int employeeId= Integer.parseInt(employeeIdText);
            ResultSet rs= timelog.getEmployeebyID(employeeId);


            if (rs.next()){
                //displays the employee ID
                String firstName= rs.getString("employee_FirstName");
                String lastName = rs.getString("employee_LastName");
                String department = rs.getString("employee_Department");

                employeeInfoName.setText(firstName + " "+ lastName);
                employeeInfoId.setText(String.valueOf(employeeId));
                employeeInfoDept.setText(department);
                employeeInfoPane.setVisible(true);

                //this code will record time-in of the employee
                LocalDateTime present =LocalDateTime.now();

                if(timelog.hasTimedInToday(employeeId)) {
                    statusLabel.setText("You already timed in today!");
                } else {
                    timelog.recordTimeIn(employeeId, present);
                    loginInfo.setText("LOG-IN TIME: " + present.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    statusLabel.setText("Data Successfully Recorded!");
                    }
            }
            else{
                ShowEmployeeNotFound();
            }
            rs.close();

        } catch (SQLException e) {
            statusLabel.setText("Database error:" + e.getMessage());
            e.printStackTrace();
        }
        catch (NumberFormatException e){
            statusLabel.setText("Invalid Employee ID Format");
        }
        timeInEmployeeID.clear();

    }

    private void recordTimeOut() {
        String employeeIdText = timeOutEmployeeID.getText().trim();

        if (employeeIdText.isEmpty()) {
            statusLabel.setText("Please enter your Employee ID");
            return;
        }

        try {
            int employeeId = Integer.parseInt(employeeIdText);
            ResultSet rs = timelog.getEmployeebyID(employeeId);

            if (rs.next()) {
                String firstName = rs.getString("employee_FirstName");
                String lastName = rs.getString("employee_LastName");
                String department = rs.getString("employee_Department");

                employeeInfoName.setText(firstName + " " + lastName);
                employeeInfoId.setText(String.valueOf(employeeId));
                employeeInfoDept.setText(department);
                employeeInfoPane.setVisible(true);

                LocalDateTime now = LocalDateTime.now();


                if (!timelog.hasTimedInToday(employeeId)) {
                    statusLabel.setText("You haven't timed in today!");
                    return;
                }

                if (timelog.hasTimedOutToday(employeeId)) {
                    statusLabel.setText("You already timed out today!");
                    return;
                }

                Time timeIn = timelog.getTimeIn(employeeId);
                if (timeIn == null) {
                    statusLabel.setText("No time-in record found for today!");
                    return;
                }

                LocalTime in = timeIn.toLocalTime();
                LocalTime out = now.toLocalTime();
                double totalHours = java.time.Duration.between(in, out).toMinutes() / 60.0;


                AttendanceStatus attendanceStatus;
                if (totalHours == 8) {
                    attendanceStatus = AttendanceStatus.Present;
                } else if (totalHours >= 4) {
                    attendanceStatus = AttendanceStatus.Undertime;
                } else {
                    attendanceStatus = AttendanceStatus.Overtime;
                }


                timelog.recordTimeOut(employeeId, now, totalHours, attendanceStatus.name().replace("_", " "));


                loginInfo.setText("LOG-OUT TIME: " + now.format(timeFormatter));
                statusLabel.setText("Time-out recorded! Status: " + attendanceStatus.name().replace("_", " "));

            } else {
                ShowEmployeeNotFound();
            }

            rs.close();

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Employee ID format");
        }

        timeOutEmployeeID.clear();

    }



    private void ShowEmployeeNotFound() {
    statusLabel.setText("Employee Does Not Exist");
    loginInfo.setText(" ");
    employeeInfoName.setText(" ");
    employeeInfoId.setText(" ");
    employeeInfoDept.setText(" ");
    employeeInfoPane.setVisible(false);

}


private void updateDateTime() {
     LocalDateTime now= LocalDateTime.now();
     timeLabel.setText(now.format(timeFormatter));
     dateLabel.setText(now.format(dateFormatter));
    }

}

