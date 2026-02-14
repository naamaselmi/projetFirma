package marketplace.GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MarketplaceController implements Initializable {

    @FXML
    private ImageView imgEquipements;

    @FXML
    private ImageView imgFournisseurs;

    @FXML
    private ImageView imgTerrains;

    @FXML
    private ImageView imgVehicules;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load images
        try {
            // These paths need to be verified against user's resource folder
            // Using placeholders or logical names
            setImage(imgEquipements, "/image/i4.png");
            setImage(imgTerrains, "/image/i2.png");
            setImage(imgVehicules, "/image/i1.png");
            setImage(imgFournisseurs, "/image/i3.png");
        } catch (Exception e) {
            System.err.println("Images not found or error loading them.");
        }
    }

    private void setImage(ImageView imageView, String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            imageView.setImage(new Image(url.toExternalForm()));
        }
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));

            // Navigate by replacing the content in the Main Dashboard
            // We need to access the AdminDashboardController's contentArea or similar
            // mechanism
            // Since we are inside a hierarchy, we can try to find the parent container

            // Current approach: Get the source Node, find the parent StackPane (Content
            // Area)
            // The source is the Button, its parent is VBox, its parent is GridPane, its
            // parent *should* be the StackPane from AdminDashboard

            // Better approach: Since navigation logic handles content replacement, we can
            // do it here.

            // Traverse up to find StackPane
            javafx.scene.Node node = (javafx.scene.Node) event.getSource();
            while (node != null && !(node instanceof StackPane)) {
                node = node.getParent();
            }

            if (node instanceof StackPane) {
                StackPane contentArea = (StackPane) node;
                contentArea.getChildren().setAll(view);
            } else {
                System.err.println("Could not find Content Area (StackPane) to navigate.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlPath);
        }
    }

    // Helper for placeholder navigation
    private void showPlaceholder(ActionEvent event, String title) {
        javafx.scene.Node node = (javafx.scene.Node) event.getSource();
        while (node != null && !(node instanceof StackPane)) {
            node = node.getParent();
        }

        if (node instanceof StackPane) {
            StackPane contentArea = (StackPane) node;
            Label label = new Label("Gestion " + title);
            label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(label);
        }
    }

    @FXML
    void handleEquipements(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/EquipementView.fxml");
    }

    @FXML
    void handleFournisseurs(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/FournisseurView.fxml");
    }

    @FXML
    void handleTerrains(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/TerrainView.fxml");
    }

    @FXML
    void handleVehicules(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/VehiculeView.fxml");
    }
}
