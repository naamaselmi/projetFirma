package marketplace.GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button btnAccueil;

    @FXML
    private Button btnEvenement;

    @FXML
    private Button btnForum;

    @FXML
    private Button btnMarketplace;

    @FXML
    private Button btnTechnicien;

    @FXML
    private Button btnUtilisateur;

    @FXML
    private ImageView logoImageView;

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load Logo if available
        try {
            // Adjust path as needed based on resources folder structure
            // Assuming image is in /image/logo.png or similar, need to verify user's logo
            // path
            // For now, leaving empty or placeholder
            URL logoUrl = getClass().getResource("/image/logo.png");
            if (logoUrl != null) {
                logoImageView.setImage(new Image(logoUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Logo not found");
        }

        // Default View
        handleMarketplace(new ActionEvent(btnMarketplace, null));
    }

    private void resetButtonStyles() {
        String defaultStyle = "sidebar-button";
        btnAccueil.getStyleClass().remove("sidebar-button-active");
        btnEvenement.getStyleClass().remove("sidebar-button-active");
        btnForum.getStyleClass().remove("sidebar-button-active");
        btnMarketplace.getStyleClass().remove("sidebar-button-active");
        btnTechnicien.getStyleClass().remove("sidebar-button-active");
        btnUtilisateur.getStyleClass().remove("sidebar-button-active");

        btnAccueil.getStyleClass().add(defaultStyle);
        btnEvenement.getStyleClass().add(defaultStyle);
        btnForum.getStyleClass().add(defaultStyle);
        btnMarketplace.getStyleClass().add(defaultStyle);
        btnTechnicien.getStyleClass().add(defaultStyle);
        btnUtilisateur.getStyleClass().add(defaultStyle);
    }

    private void setActiveButton(Button button) {
        resetButtonStyles();
        button.getStyleClass().add("sidebar-button-active");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentArea.getChildren().removeAll();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlPath);
        }
    }

    private void loadPlaceholder(String sectionName) {
        Label label = new Label("This is gestion " + sectionName);
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    @FXML
    void handleAccueil(ActionEvent event) {
        setActiveButton(btnAccueil);
        loadPlaceholder("Accueil");
    }

    @FXML
    void handleEvenement(ActionEvent event) {
        setActiveButton(btnEvenement);
        loadPlaceholder("Évènement");
    }

    @FXML
    void handleForum(ActionEvent event) {
        setActiveButton(btnForum);
        loadPlaceholder("Forum");
    }

    @FXML
    void handleMarketplace(ActionEvent event) {
        setActiveButton(btnMarketplace);
        // Load Marketplace View
        loadView("/marketplace/GUI/views/MarketplaceView.fxml");
    }

    @FXML
    void handleTechnicien(ActionEvent event) {
        setActiveButton(btnTechnicien);
        loadPlaceholder("Technicien");
    }

    @FXML
    void handleUtilisateur(ActionEvent event) {
        setActiveButton(btnUtilisateur);
        loadPlaceholder("Utilisateur");
    }
}
