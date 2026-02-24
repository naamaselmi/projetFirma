package com.examen.firmapi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/examen/firmapi/auth-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 800, 500);
        stage.setTitle("FIRMA - User Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
