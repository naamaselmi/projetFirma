package Firma.controllers.GestionMarketplace;

import Firma.tools.GestionEvenement.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for the client-side FIRMA Welcome / Accueil page.
 */
public class ClientAccueilController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblSubtitle;
    @FXML private Label lblDate;

    // Shortcut cards
    @FXML private HBox shortcutRow;

    // Services sections
    @FXML private VBox eventsServiceBox;
    @FXML private VBox marketServiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadWelcome();
        buildShortcuts();
    }

    // ────────────────────────────────────────────────
    //  WELCOME HEADER
    // ────────────────────────────────────────────────
    private void loadWelcome() {
        try {
            var user = SessionManager.getInstance().getUtilisateur();
            if (user != null) {
                lblWelcome.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom() + " 👋");
            } else {
                lblWelcome.setText("Bienvenue sur FIRMA 👋");
            }
        } catch (Exception e) {
            lblWelcome.setText("Bienvenue sur FIRMA 👋");
        }
        lblDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
    }

    // ────────────────────────────────────────────────
    //  SHORTCUT CARDS  (fire sidebar buttons)
    // ────────────────────────────────────────────────
    private void buildShortcuts() {
        shortcutRow.getChildren().clear();
        shortcutRow.setAlignment(Pos.CENTER);
        shortcutRow.setSpacing(20);

        String[][] shortcuts = {
            {"📅  Événements",  "#49ad32", "#btnEvenement"},
            {"🛒  Marketplace", "#e67e22", "#btnMarketplace"},
            {"💬  Forum",       "#3498db", "#btnForum"},
            {"🔧  Technicien",  "#9b59b6", "#btnTechnicien"},
        };

        for (String[] s : shortcuts) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(195);
            card.setPrefHeight(65);
            card.setPadding(new Insets(10, 15, 10, 15));
            card.setStyle(
                "-fx-background-color: " + s[1] + ";" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"
            );

            Label lbl = new Label(s[0]);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
            card.getChildren().add(lbl);

            String base = card.getStyle();
            card.setOnMouseEntered(e -> card.setStyle(base + "-fx-opacity: 0.85;"));
            card.setOnMouseExited(e -> card.setStyle(base));

            String sidebarId = s[2];
            card.setOnMouseClicked(e -> {
                Button btn = (Button) shortcutRow.getScene().lookup(sidebarId);
                if (btn != null) btn.fire();
            });

            shortcutRow.getChildren().add(card);
        }
    }
}
