package com.example.jopay;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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


 private final DateTimeFormatter timeFormatter= DateTimeFormatter.ofPattern("HH:mm:ss");
 private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EE, MMMM dd, yyyy");

 private DatabaseConnector connect = new DatabaseConnector();



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
         Connection conn= DriverManager.getConnection(URL, User, Password);

         String query= "SELECT * FROM employee_info WHERE employee_id = ?";
         PreparedStatement stmt = conn.prepareStatement(query);
         stmt.setInt(1, employeeId);
         ResultSet rs= stmt.executeQuery();

         if (rs.next()){
             //displays the employee ID
             String firstName= rs.getString("employee_FirstName");
             String lastName = rs.getString("employee_LastName");
             String department = rs.getString("employee_dept");

             employeeInfoName.setText(firstName + " "+ lastName);
             employeeInfoId.setText(String.valueOf(employeeId));
             employeeInfoDept.setText(department);
             EmployeeInfoVisibile(true);




             //this code will record time-in of the employee

             String insert= "INSERT INTO time_logs (employee_id, time_in) VALUES (?, NOW()) ";
             PreparedStatement insertStmt= conn.prepareStatement(insert);
             insertStmt.setInt(1, employeeId);
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
             EmployeeInfoVisibile(false);

         }
      rs.close();
         stmt.close();
         conn.close();

     } catch (SQLException e) {
         statusLabel.setText("Database error:" + e.getMessage());
         e.printStackTrace();
     }
     timeInEmployeeID.clear();


    }

    private void EmployeeInfoVisibile(boolean visible)
    {
        employeeInfo_panel.setVisible(visible);
        employeeNameLabel.setVisible(visible);
        employeeIDLabel.setVisible(visible);
        employeeDeptLabel.setVisible(visible);
        loginInfo.setVisible(visible);
        employeeInfoLabel.setVisible(visible);

    }

    private void updateDateTime() {
     LocalDateTime now= LocalDateTime.now();
     timeLabel.setText(now.format(timeFormatter));
     dateLabel.setText(now.format(dateFormatter));
    }


}

