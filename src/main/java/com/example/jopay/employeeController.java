package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class employeeController {

    @FXML
    private ComboBox<String> comboBoxMonth;

    @FXML
    private Button btnDownload;

    @FXML
    private Hyperlink updatePasswordLink;

    @FXML
    private Hyperlink logoutLink;

    @FXML
    private ImageView logoutImageView;

    private String selectedPeriod = "";

    @FXML
    private Button updateBackBtn;

    @FXML
    void initialize() {
        comboBoxMonth.getItems().addAll(
                "2024 Dec 26 - 2025 Jan 10",
                "2025 Jan 11 - Jan 25",
                "2025 Jan 26 - Feb 10",
                "2025 Feb 11 - Feb 25",
                "2025 Feb 26 - Mar 10",
                "2025 Mar 11 - Mar 25",
                "2025 Mar 26 - Apr 10",
                "2025 Apr 11 - Apr 25",
                "2025 Apr 26 - May 10",
                "2025 May 11 - May 25",
                "2025 May 26 - Jun 10",
                "2025 Jun 11 - Jun 25",
                "2025 Jun 26 - Jul 10",
                "2025 Jul 11 - Jul 25",
                "2025 Jul 26 - Aug 10",
                "2025 Aug 11 - Aug 25",
                "2025 Aug 26 - Sept 10",
                "2025 Sept 11 - Sept 25",
                "2025 Sept 26 - Oct 10",
                "2025 Oct 11 - Oct 25",
                "2025 Oct 26 - Nov 10",
                "2025 Nov 11 - Nov 25"
        );

        comboBoxMonth.setPromptText("2025");
        btnDownload.setVisible(false);
    }

    @FXML
    void handleMonthSelection() {
        selectedPeriod = comboBoxMonth.getValue();

        if (selectedPeriod != null && !selectedPeriod.isEmpty()) {
            btnDownload.setVisible(true);
            System.out.println("Selected period: " + selectedPeriod);
        } else {
            btnDownload.setVisible(false);
        }
    }

    @FXML
    void handleDownloadPayslip() {
        if (selectedPeriod == null || selectedPeriod.isEmpty()) {
            showAlert("Please select a month first!");
            return;
        }

        try {
            String employeeId = "11111";
            String filename = "payslips/" + employeeId + "_" + selectedPeriod + ".pdf";
            File pdfFile = new File(filename);

            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
                showAlert("Opening payslip for " + selectedPeriod);
            } else {
                showAlert("Payslip not found for " + selectedPeriod + "\n\nFile: " + filename);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error opening PDF file!");
        }
    }

    @FXML
    void handleUpdatePassword() throws IOException {
        FXMLLoader updatePasswordLoader = new FXMLLoader(getClass().getResource("updatepassword.fxml"));
        Stage stage = (Stage) updatePasswordLink.getScene().getWindow();
        Scene updatePasswordScene = new Scene(updatePasswordLoader.load());
        stage.setScene(updatePasswordScene);
        stage.show();
    }

    @FXML
    void handleLogout() throws IOException {
        FXMLLoader employeeLoginLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) logoutLink.getScene().getWindow();
        Scene employeeLoginScene = new Scene(employeeLoginLoader.load());
        stage.setScene(employeeLoginScene);
        stage.show();
    }

    @FXML
    void handleLogoutImageClick() throws IOException {
        FXMLLoader employeeLoginLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) logoutImageView.getScene().getWindow();
        Scene employeeLoginScene = new Scene(employeeLoginLoader.load());
        stage.setScene(employeeLoginScene);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Payslip");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void updateBackButton() throws IOException {
        FXMLLoader employeeDashboardBack = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Stage stage = (Stage) updateBackBtn.getScene().getWindow();
        Scene employeeDashboardbackScene = new Scene(employeeDashboardBack.load());
        stage.setScene(employeeDashboardbackScene);
        stage.show();
    }

    @FXML
    void updatePassword() throws IOException {
        FXMLLoader employeeDashboardBack = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Stage stage = (Stage) updateBackBtn.getScene().getWindow();
        Scene employeeDashboardbackScene = new Scene(employeeDashboardBack.load());
        stage.setScene(employeeDashboardbackScene);
        stage.show();
    }
}