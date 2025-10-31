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
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class employeeController {

    @FXML
    private ComboBox<String> comboBoxPeriod;

    @FXML
    private Hyperlink downloadLink;

    @FXML
    private ImageView downloadBtn;

    @FXML
    private Hyperlink updatePasswordLink;

    @FXML
    private Hyperlink logoutLink;

    @FXML
    private ImageView logoutImageView;

    private String selectedPeriod = "";

    @FXML
    private Button updateBackBtn;

    // Employee data
    private String employeeId = "11111";
    private String employeeName = "Coco Martin";
    private String employmentStatus = "Regular";

    // Earnings
    private double basicPay = 7650.00;
    private double overtime = 1995.29;

    // Allowances
    private double telecom = 500.00;
    private double travel = 500.00;
    private double riceSubsidy = 1000.00;
    private double nonTaxableSalary = 0.00;
    private double perDiem = 3600.00;

    // Deductions
    private double sssContributions = 400.00;
    private double phicContributions = 191.25;
    private double hdmfContributions = 200.00;
    private double sssLoan = 323.02;
    private double absences = 0.00;
    private double withholdingTax = 0.00;

    @FXML
    void initialize() {
        comboBoxPeriod.getItems().addAll(
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

        comboBoxPeriod.setPromptText("Select period");
        downloadLink.setVisible(false);
        downloadBtn.setVisible(false);

        File payslipsDir = new File("payslips");
        if (!payslipsDir.exists()) {
            payslipsDir.mkdir();
        }
    }

    @FXML
    void handleMonthSelection() {
        selectedPeriod = comboBoxPeriod.getValue();

        if (selectedPeriod != null && !selectedPeriod.isEmpty()) {
            downloadLink.setVisible(true);
            downloadBtn.setVisible(true);
        } else {
            downloadLink.setVisible(false);
            downloadBtn.setVisible(false);
        }
    }

    @FXML
    void handleDownloadPayslip() {
        if (selectedPeriod == null || selectedPeriod.isEmpty()) {
            showAlert("Please select a month first!");
            return;
        }

        try {
            String filename = "payslips/" + employeeId + "_" + selectedPeriod + "_payslip.pdf";
            File pdfFile = new File(filename);

            generatePayslipPDF(pdfFile);

            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
                showAlert("Payslip generated and opened successfully!");
            } else {
                showAlert("Error: PDF file was not created.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating PDF file: " + e.getMessage());
        }
    }

    private void generatePayslipPDF(File file) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        Image photo = Image.getInstance("/Users/kirstencruz/Documents/college/secondyear/CS21S1/cs 002 - OOP/finals/jojopaypay/Jopay/src/main/resources/com/example/jopay/logo.png");

        photo.scaleToFit(100, 120);
        photo.setAlignment(Element.ALIGN_CENTER);
        document.add(photo);
        document.add(new Paragraph("\n", normalFont));

        Paragraph empName = new Paragraph("Employee Name: " + employeeName, normalFont);
        empName.setSpacingAfter(5);
        document.add(empName);

        Paragraph period = new Paragraph("Period: " + selectedPeriod, normalFont);
        period.setSpacingAfter(5);
        document.add(period);

        Paragraph empStatus = new Paragraph("Employment Status: " + employmentStatus, normalFont);
        empStatus.setSpacingAfter(15);
        document.add(empStatus);

        PdfPTable mainTable = new PdfPTable(4);
        mainTable.setWidthPercentage(100);
        float[] columnWidths = {35f, 15f, 35f, 15f};
        mainTable.setWidths(columnWidths);

        addCell(mainTable, "Basic Pay", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(basicPay), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);

        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Overtime", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(overtime), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Allowance and De Minimis", boldFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Deductions", boldFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);

        addCell(mainTable, "Telecom", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(telecom), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "SSS Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(sssContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Travel", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(travel), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "PHIC Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(phicContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Rice subsidy", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(riceSubsidy), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "HDMF Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(hdmfContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Non-taxable salary", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(nonTaxableSalary), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "SSS Loan", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(sssLoan), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Per Diem", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(perDiem), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);

        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Absences", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(absences), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Withholding Tax", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(withholdingTax), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        document.add(mainTable);

        double grossPay = basicPay + overtime + telecom + travel + riceSubsidy + perDiem;
        double totalDeductions = sssContributions + phicContributions + hdmfContributions + sssLoan + absences + withholdingTax;
        double netPay = grossPay - totalDeductions;

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(20);
        float[] summaryWidths = {75f, 25f};
        summaryTable.setWidths(summaryWidths);

        addCell(summaryTable, "Gross Pay", boldFont, Rectangle.BOX);
        addCell(summaryTable, formatAmount(grossPay), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(summaryTable, "Total Deductions & Taxes", boldFont, Rectangle.BOX);
        addCell(summaryTable, formatAmount(totalDeductions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        PdfPCell netPayLabel = new PdfPCell(new Phrase("Net Pay", boldFont));
        netPayLabel.setPadding(5);
        netPayLabel.setBorder(Rectangle.BOX);
        summaryTable.addCell(netPayLabel);

        Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED);
        PdfPCell netPayAmount = new PdfPCell(new Phrase(formatAmount(netPay), redFont));
        netPayAmount.setPadding(5);
        netPayAmount.setBorder(Rectangle.BOX);
        netPayAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(netPayAmount);

        document.add(summaryTable);

        document.close();
    }

    private void addCell(PdfPTable table, String text, Font font, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(border);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, int border, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(border);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private String formatAmount(double amount) {
        if (amount == 0.00) {
            return "0.00";
        }
        return String.format("%,.2f", amount);
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
}