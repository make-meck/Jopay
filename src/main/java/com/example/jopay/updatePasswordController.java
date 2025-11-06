package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class updatePasswordController {

    @FXML
    private TextField employee_id;

    @FXML
    private PasswordField pass_word;  // Old Password

    @FXML
    private PasswordField pass_word1;  // New Password

    @FXML
    private PasswordField pass_word11;  // Confirm Password

    @FXML
    private Label error;

    @FXML
    private Button backButton;

    @FXML
    private Button loginButton;  // UPDATE button

    private int employeeID = 11111;
    private String currentPassword = "0000";

    private final EmployeeDAO  employeeDAO= new EmployeeDAO();

    @FXML
    void updateBackButton() throws IOException {
        FXMLLoader employeeDashboardLoader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene employeeDashboardScene = new Scene(employeeDashboardLoader.load());
        stage.setScene(employeeDashboardScene);
        stage.show();
    }

    /*@FXML
    void updatePassword() throws IOException {
        try {
            if (employee_id.getText().isEmpty() || pass_word.getText().isEmpty() ||
                    pass_word1.getText().isEmpty() || pass_word11.getText().isEmpty()) {
                error.setText("Please fill in all fields.");
                return;
            }

            if (Integer.parseInt(employee_id.getText()) != employeeID) {
                error.setText("Invalid Employee ID.");
                return;
            }

            if (!pass_word.getText().equals(currentPassword)) {
                error.setText("Incorrect old password.");
                return;
            }

            if (!pass_word1.getText().equals(pass_word11.getText())) {
                error.setText("New passwords do not match.");
                return;
            }

            if (pass_word.getText().equals(pass_word1.getText())) {
                error.setText("New password must be different from old password.");
                return;
            }

            currentPassword = pass_word1.getText();

            showAlert("Password updated successfully!");

            FXMLLoader employeeDashboardLoader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene employeeDashboardScene = new Scene(employeeDashboardLoader.load());
            stage.setScene(employeeDashboardScene);
            stage.show();

        } catch (NumberFormatException e) {
            error.setText("Invalid Employee ID format.");
        }
    } */

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

            // Fetch employee from database
            //taga-kuha ng info sa database
            Optional<Employee> employeeOpt = employeeDAO.findEmployeeId( EmpID);
            if (employeeOpt.isEmpty()) {
                error.setText("Employee ID not found.");
                return;
            }

            Employee employee = employeeOpt.get();

            // Verify old password
            // iveverify niya ung dating password na nasa database
            if (!employee.getPassword().equals(oldPassword)) {
                error.setText("Incorrect old password.");
                return;
            }

            //checks the new and confirm password if same sila
            if (!newPassword.equals(confirmPassword)) {
                error.setText("New passwords do not match.");
                return;
            }

            //preventing the user para magamit ung dati nilang password
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
                Scene scene = new Scene(loader.load());

                employeeController controller = loader.getController();
                controller.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();

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