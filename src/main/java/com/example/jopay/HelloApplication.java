package com.example.jopay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
       // FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("employee_timein.fxml"));
        FXMLLoader adminLoader = new FXMLLoader(HelloApplication.class.getResource("employeelogin.fxml"));
        Scene scene = new Scene(adminLoader.load());

        Image icon = new Image(getClass().getResource("/com/example/jopay/logoIcon.png").toExternalForm());
        stage.getIcons().add(icon);
        stage.setTitle("JOPAY");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}