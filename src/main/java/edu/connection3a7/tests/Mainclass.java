package edu.connection3a7.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Mainclass extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uploads/AjouterDemandeBack.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dashboard Technicien");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
