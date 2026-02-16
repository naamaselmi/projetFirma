package edu.connection3a7.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationBack {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        System.out.println("✅ Primary stage initialisé: " + stage);
    }

    // ========== NAVIGATION PRINCIPALE ==========

    public static void goToDashboard() {
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null! Appelez setPrimaryStage d'abord");
            return;
        }
        loadPage("/uploads/DashboardTech.fxml", "Dashboard - Technicien");
    }

    public static void goToListeDemandesBack() {
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null!");
            return;
        }
        loadPage("/uploads/AjouterDemandeBack.fxml", "Gestion des demandes");
    }

    public static void goToMesInterventions() {
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null!");
            return;
        }
        loadPage("/uploads/MesInterventions.fxml", "Mes interventions");
    }

    public static void goToProfilTechnicien() {
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null!");
            return;
        }
        loadPage("/uploads/ProfilTechnicien.fxml", "Mon profil");
    }

    public static void retourAccueil() {
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null!");
            return;
        }
        loadPage("/uploads/AccueilPrincipal.fxml", "FIRMA - Accueil");
    }

    // ========== OUVERTURE DÉTAIL DEMANDE (POPUP) ==========

    public static void openDetailDemande(int idDemande, boolean isTechnicien) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationBack.class.getResource("/uploads/DetailDemande.fxml")
            );
            Parent root = loader.load();

            DetailDemandeController controller = loader.getController();
            controller.loadDemandeById(idDemande, isTechnicien);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Détail de la demande #" + idDemande);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement DetailDemande.fxml");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== UTILITAIRE ==========

    private static void loadPage(String fxmlPath, String title) {
        try {
            System.out.println("📂 Chargement: " + fxmlPath);

            // Vérifier que le fichier existe
            var resource = NavigationBack.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            primaryStage.setTitle(title);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            System.out.println("✅ Page chargée: " + title);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void logout() {
        try {
            var resource = NavigationBack.class.getResource("/uploads/Login.fxml");
            if (resource == null) {
                System.err.println("❌ Fichier Login.fxml introuvable");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            primaryStage.setTitle("Connexion - FIRMA");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}