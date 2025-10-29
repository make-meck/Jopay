package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class employeeLoginController {

    @FXML
    private TextField employee_id;

    @FXML
    private PasswordField pass_word;

    @FXML
    private Label error;

    @FXML
    private Button loginButton;

    @FXML
    private Button backButton;

    @FXML
    private Button adminButton;

    private int employeeID = 11111;
    private String password = "0000";

    @FXML
    private void loginClick() throws IOException {
        try {
            if (employee_id.getText().isEmpty() || pass_word.getText().isEmpty()) {
                error.setText("Please fill in all fields.");
                return;
            }

            if (Integer.parseInt(employee_id.getText()) != employeeID || !pass_word.getText().equals(password)) {
                error.setText("Account not found. Please try again.");
            } else {
                FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene dashboardScene = new Scene(dashboardLoader.load());
                stage.setScene(dashboardScene);
                stage.show();
            }
        } catch (NumberFormatException e) {
            error.setText("Invalid Employee ID format.");
        }
    }

    /*@FXML
    private void backClick() throws IOException {
        FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource("welcome.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene welcomeScene = new Scene(welcomeLoader.load());
        stage.setScene(welcomeScene);
        stage.show();
    }*/

    @FXML
    private void adminIconClick() throws IOException {
        FXMLLoader adminLoginLoader = new FXMLLoader(getClass().getResource("admin2.fxml"));
        Stage stage = (Stage) adminButton.getScene().getWindow();
        Scene adminLoginScene = new Scene(adminLoginLoader.load());
        stage.setScene(adminLoginScene);
        stage.show();
    }
}