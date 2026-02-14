package marketplace.GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

/**
 * Controller for the Login screen
 */
public class LoginController {

    @FXML
    private ImageView logoImage;
    @FXML
    private ImageView sloganImage;
    @FXML
    private ImageView firmaImage;
    @FXML
    private Button cancelButton;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginMessage;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;

    @FXML
    public void initialize() {
        // Load images using JavaFX resource loading
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/image/logo.png")));
            sloganImage.setImage(new Image(getClass().getResourceAsStream("/image/slogan.png")));
            firmaImage.setImage(new Image(getClass().getResourceAsStream("/image/firma.png")));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancelButtonOnClick(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void loginButtonOnClick(ActionEvent e) {
        // Validate input fields
        if (usernameTextField.getText().isEmpty() || passwordTextField.getText().isEmpty()) {
            loginMessage.setStyle("-fx-text-fill: #ff0000;");
            loginMessage.setText("Please enter both email and password!");
            return;
        }

        try {
            marketplace.service.UtilisateurService utilisateurService = new marketplace.service.UtilisateurService();
            marketplace.entities.Utilisateur utilisateur = utilisateurService.login(usernameTextField.getText(),
                    passwordTextField.getText());

            if (utilisateur != null) {
                // Login successful
                loginMessage.setStyle("-fx-text-fill: #088002;");
                loginMessage.setText("Welcome " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ("
                        + utilisateur.getTypeUser() + ")!");

                // Navigate to dashboard based on user role
                navigateToDashboard(utilisateur.getTypeUser());
            } else {
                // Login failed
                loginMessage.setStyle("-fx-text-fill: #ff0000;");
                loginMessage.setText("Invalid email or password!");
            }
        } catch (Exception ex) {
            loginMessage.setStyle("-fx-text-fill: #ff0000;");
            loginMessage.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Navigate to the appropriate dashboard based on user role
     * 
     * @param userType The type of user (admin or client)
     */
    private void navigateToDashboard(String userType) {
        try {
            String fxmlFile;

            // Determine which dashboard to load based on user type
            if ("admin".equalsIgnoreCase(userType)) {
                fxmlFile = "/marketplace/GUI/views/AdminDashboard.fxml";
            } else {
                fxmlFile = "/marketplace/GUI/views/client_dashboard.fxml";
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Create new scene with the dashboard
            Scene scene = new Scene(root, 800, 600);

            // Set fullscreen
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle(userType.toUpperCase() + " Dashboard");
            stage.show();

            System.out.println("Successfully navigated to " + userType + " dashboard");

        } catch (Exception e) {
            System.err.println("Error navigating to dashboard: " + e.getMessage());
            e.printStackTrace();
            loginMessage.setStyle("-fx-text-fill: #ff0000;");
            loginMessage.setText("Error loading dashboard!");
        }
    }
}
