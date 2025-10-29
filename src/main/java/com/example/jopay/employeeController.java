package com.example.jopay;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class employeeController {

    @FXML
    private ComboBox<String> comboBoxMonth;

    @FXML
    private Button btnDownload;

    private String selectedMonth = "";

    /**
     * Initialize method - runs when FXML loads
     */
    @FXML
    void initialize() {
        // Add month options to ComboBox
        comboBoxMonth.getItems().addAll(
                "January 2025",
                "February 2025",
                "March 2025",
                "April 2025",
                "May 2025",
                "June 2025",
                "July 2025",
                "August 2025",
                "September 2025",
                "October 2025",
                "November 2025",
                "December 2025"
        );

        // Set prompt text
        comboBoxMonth.setPromptText("Select Month");

        // Make sure download button is hidden initially
        btnDownload.setVisible(false);
    }

    /**
     * Called when user selects a month from ComboBox
     */
    @FXML
    void handleMonthSelection() {
        selectedMonth = comboBoxMonth.getValue();

        if (selectedMonth != null && !selectedMonth.isEmpty()) {
            // Show the download button
            btnDownload.setVisible(true);
            System.out.println("Selected month: " + selectedMonth);
        } else {
            // Hide download button if nothing selected
            btnDownload.setVisible(false);
        }
    }

    /**
     * Called when user clicks Download PDF button
     */
    @FXML
    void handleDownloadPayslip() {
        if (selectedMonth == null || selectedMonth.isEmpty()) {
            showAlert("Please select a month first!");
            return;
        }

        try {
            // Get employee ID (you can get this from login session or database)
            String employeeId = "EMP001"; // Replace with actual employee ID

            // Create filename based on selected month
            // Example: payslips/EMP001_January_2025.pdf
            String monthName = selectedMonth.replace(" ", "_");
            String filename = "payslips/" + employeeId + "_" + monthName + ".pdf";

            File pdfFile = new File(filename);

            // Check if file exists
            if (pdfFile.exists()) {
                // Open the PDF
                Desktop.getDesktop().open(pdfFile);
                showAlert("Opening payslip for " + selectedMonth);
            } else {
                showAlert("Payslip not found for " + selectedMonth + "\n\nFile: " + filename);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error opening PDF file!");
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Payslip");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}