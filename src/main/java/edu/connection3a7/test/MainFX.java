package edu.connection3a7.test;

import edu.connection3a7.controllers.LoginController;
import edu.connection3a7.tools.TicketServerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage)  {
        // Démarrer le serveur de tickets pour les QR Codes mobiles
        TicketServerService.getInstance().demarrer();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginApplication.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println("Erreur de l'application"+e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Arrêter le serveur de tickets proprement à la fermeture
        TicketServerService.getInstance().arreter();
    }
}
