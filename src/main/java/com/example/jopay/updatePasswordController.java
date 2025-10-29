package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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

    @FXML
    void updateBackButton() throws IOException {
        FXMLLoader employeeDashboardLoader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene employeeDashboardScene = new Scene(employeeDashboardLoader.load());
        stage.setScene(employeeDashboardScene);
        stage.show();
    }

    @FXML
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
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Password");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}