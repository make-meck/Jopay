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
 private Pane employeeInfoPane;


 private final DateTimeFormatter timeFormatter= DateTimeFormatter.ofPattern("HH:mm:ss");
 private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EE, MMMM dd, yyyy");

  private final TimelogModel timelog= new TimelogModel();



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
            timelog.recordTimeIn(employeeId, present);

             loginInfo.setText("LOG-IN TIME: " + present.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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

     } catch (SQLException e) {
         statusLabel.setText("Database error:" + e.getMessage());
         e.printStackTrace();
     }
     catch(NumberFormatException e){
         statusLabel.setText("Invalid Employee ID Format");
     }
     timeInEmployeeID.clear();


    }

    private void updateDateTime() {
     LocalDateTime now= LocalDateTime.now();
     timeLabel.setText(now.format(timeFormatter));
     dateLabel.setText(now.format(dateFormatter));
    }


}

