package com.example.jopay;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage. Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
 private Rectangle employeeInfo_panel;
 @FXML
    private Label employeeInfoLabel;
 @FXML
    private Label employeeNameLabel;
 @FXML
 private Label employeeIDLabel;
 @FXML
 private Label employeeDeptLabel;
 @FXML
 private Pane employeeInfoPane;


 private final DateTimeFormatter timeFormatter= DateTimeFormatter.ofPattern("HH:mm:ss");
 private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EE, MMMM dd, yyyy");

  DatabaseConnector connect = new DatabaseConnector();



 @FXML
    public void initialize(){
     updateDateTime();

     Timeline clock= new Timeline( new KeyFrame(Duration.seconds(1), event -> updateDateTime())
     );
     clock.setCycleCount(Timeline.INDEFINITE);
     clock.play();

     timeInEmployeeID.setOnAction(event -> recordTimeIn());
 }

    private void recordTimeIn() {
     String employeeIdText= timeInEmployeeID.getText().trim();

     if(employeeIdText.isEmpty()){
         statusLabel.setText("Please enter you Employee ID");
         return;
     }

     try{
         int employeeId= Integer.parseInt(employeeIdText);
        connect.getConnection();

         String query= "SELECT * FROM employee_info WHERE employee_id = ?";
         PreparedStatement stmt = connect.prepareStatement(query);
         stmt.setInt(1, employeeId);
         ResultSet rs= stmt.executeQuery();

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
            Date date = Date.valueOf(present.toLocalDate()); //converts to java.sqldate
            Time time = Time.valueOf(present.toLocalTime()); // converts to java.sql time


             String insert = "INSERT INTO time_log (employee_id, log_date, time_in) VALUES (?, ?, ?)";
             PreparedStatement insertStmt= connect.prepareStatement(insert);
             insertStmt.setInt(1, employeeId);
             insertStmt.setDate(2, date);
             insertStmt.setTime(3, time);
             insertStmt.executeUpdate();

             loginInfo.setText("LOG-IN TIME: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
             statusLabel.setText("Data Successfully Recorded!");
         }
         else {
             statusLabel.setText("Employee Does Not Exist");
             loginInfo.setText(" ");
             employeeInfoName.setText(" ");
             employeeInfoId.setText(" ");
             employeeInfoDept.setText(" ");
             employeeInfoPane.setVisible(false);

         }
      rs.close();
         stmt.close();
         connect.close();

     } catch (SQLException e) {
         statusLabel.setText("Database error:" + e.getMessage());
         e.printStackTrace();
     }
     timeInEmployeeID.clear();


    }



    private void updateDateTime() {
     LocalDateTime now= LocalDateTime.now();
     timeLabel.setText(now.format(timeFormatter));
     dateLabel.setText(now.format(dateFormatter));
    }


}

