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
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("employeelogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Image icon = new Image(getClass().getResource("/com/example/jopay/logoIcon.png").toExternalForm());
        stage.setHeight(745);
        stage.setWidth(1250);
        stage.getIcons().add(icon);
        stage.setTitle("JOPAY");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
    }

    public static void main(String[] args) {
      autoAbsent autoabsent = new autoAbsent();
       autoabsent.scheduleDailyCheck();
        launch();
    }
}