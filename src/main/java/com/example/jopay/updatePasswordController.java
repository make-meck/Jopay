package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class updatePasswordController {

    @FXML private TextField employee_id;     // Employee ID
    @FXML private PasswordField pass_word;  // Old Password
    @FXML private PasswordField pass_word1;  // New Password
    @FXML private PasswordField pass_word11;  // Confirm Password
    @FXML private Label error;
    @FXML private Button backButton;
    @FXML private Button loginButton;



    private final EmployeeDAO  employeeDAO= new EmployeeDAO();
    private String currentEmployeeId;

    public void setEmployeeId(String employeeId){
        this.currentEmployeeId = employeeId;
    }
    @FXML
    void updateBackButton() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Parent root = loader.load();

        employeeController controller = loader.getController();
        controller.setLoggedInEmployeeId(currentEmployeeId); // Use stored ID
        controller.loadEmployeeData(currentEmployeeId); // Use stored ID

        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.getScene().setRoot(root);
    }


    @FXML
    void updatePassword() {
        String EmpID = employee_id.getText();
        String oldPassword = pass_word.getText();
        String newPassword = pass_word1.getText();
        String confirmPassword = pass_word11.getText();

        // Check if all fields are filled
        if ( EmpID.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            error.setText("Please fill in all fields.");
            return;
        }

        try {
            int id = Integer.parseInt( EmpID);

            Optional<Employee> employeeOpt = employeeDAO.findEmployeeId( EmpID);
            if (employeeOpt.isEmpty()) {
                error.setText("Employee ID not found.");
                return;
            }

            Employee employee = employeeOpt.get();

            if (!employee.getPassword().equals(oldPassword)) {
                error.setText("Incorrect old password.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                error.setText("New passwords do not match.");
                return;
            }
            if (oldPassword.equals(newPassword)) {
                error.setText("New password must be different from old password.");
                return;
            }

            // Update password in database

            boolean updated = employeeDAO.updatePassword(Integer.parseInt(EmpID), newPassword);
            if (updated){
                showAlert("Password updated successfully!");

                Employee currentEmployee = employeeDAO.findEmployeeId(EmpID).get();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
                Parent root = loader.load();

                employeeController controller = loader.getController();
                controller.setEmployeeName(currentEmployee.getFirstName() + " " + currentEmployee.getLastName());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.getScene().setRoot(root);

            }
            else {
                error.setText("Failed to update password. Try Again");
            }

        } catch (NumberFormatException e) {
            error.setText("Invalid Employee ID format. Please enter numbers only.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            error.setText("Database error while updating password.");
        } catch (IOException ex) {
            ex.printStackTrace();
            error.setText("Error loading dashboard.");
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Password");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
