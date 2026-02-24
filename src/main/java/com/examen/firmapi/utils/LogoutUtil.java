package com.examen.firmapi.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LogoutUtil {

    public static void logout(Stage stage) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    LogoutUtil.class.getResource("/com/examen/firmapi/auth-view.fxml")
            );

            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("Authentification");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}