package marketplace.GUI.Controller;

import javafx.animation.TranslateTransition;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import marketplace.service.CartService;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller for the Client Dashboard screen
 * Handles navigation between different views for regular users
 */
public class ClientDashboardController implements Initializable, CartService.CartChangeListener {

    @FXML
    private StackPane rootPane;
    @FXML
    private Button btnAccueil;
    @FXML
    private Button btnProfil;
    @FXML
    private Button btnForum;
    @FXML
    private Button btnEvenement;
    @FXML
    private Button btnTechnicien;
    @FXML
    private Button btnMarketplace;
    @FXML
    private Button btnDeconnecter;
    @FXML
    private Button btnCart;
    @FXML
    private Button btnRentals;

    @FXML
    private Label lblAccueil;
    @FXML
    private Label lblProfil;
    @FXML
    private Label lblForum;
    @FXML
    private Label lblEvenement;
    @FXML
    private Label lblTechnicien;
    @FXML
    private Label lblMarketplace;
    @FXML
    private Label lblDeconnecter;
    @FXML
    private Label cartBadge;

    @FXML
    private SVGPath iconAccueil;
    @FXML
    private SVGPath iconProfil;
    @FXML
    private SVGPath iconForum;
    @FXML
    private SVGPath iconEvenement;
    @FXML
    private SVGPath iconTechnicien;
    @FXML
    private SVGPath iconMarketplace;
    @FXML
    private SVGPath iconDeconnecter;

    @FXML
    private ImageView logoImageView;

    @FXML
    private StackPane contentArea;
    
    @FXML
    private HBox cartOverlay;
    
    @FXML
    private VBox cartPanelContainer;
    
    @FXML
    private HBox rentalsOverlay;
    
    @FXML
    private VBox rentalsPanelContainer;

    private Button currentActiveButton;
    private CartService cartService;
    private boolean isCartOpen = false;
    private CartPanelController cartPanelController;
    private boolean isRentalsOpen = false;
    private RentalsPanelController rentalsPanelController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize cart service
        cartService = CartService.getInstance();
        cartService.addCartChangeListener(this);
        
        // Load Logo
        try {
            URL logoUrl = getClass().getResource("/image/logo.png");
            if (logoUrl != null) {
                logoImageView.setImage(new Image(logoUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Logo not found");
        }

        // Load cart panel
        loadCartPanel();
        
        // Load rentals panel
        loadRentalsPanel();
        
        // Setup cart overlay click-outside-to-close
        cartOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == cartOverlay) {
                toggleCart(false);
            }
        });
        
        // Setup rentals overlay click-outside-to-close
        rentalsOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == rentalsOverlay) {
                toggleRentals(false);
            }
        });
        
        // Update cart badge
        updateCartBadge();
        
        // Default to Marketplace view
        handleMarketplace(new ActionEvent(btnMarketplace, null));
    }
    
    /**
     * Load the cart panel into the overlay
     */
    private void loadCartPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/GUI/views/CartPanelView.fxml"));
            Parent cartPanel = loader.load();
            
            cartPanelController = loader.getController();
            cartPanelController.setOnCloseCallback(() -> toggleCart(false));
            cartPanelController.setOnPayCallback(() -> {
                toggleCart(false);
                navigateToPayment();
            });
            
            cartPanelContainer.getChildren().clear();
            cartPanelContainer.getChildren().add(cartPanel);
            
        } catch (IOException e) {
            System.err.println("Could not load cart panel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load the rentals panel into the overlay
     */
    private void loadRentalsPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/GUI/views/RentalsPanelView.fxml"));
            Parent rentalsPanel = loader.load();
            
            rentalsPanelController = loader.getController();
            rentalsPanelController.setOnCloseCallback(() -> toggleRentals(false));
            
            rentalsPanelContainer.getChildren().clear();
            rentalsPanelContainer.getChildren().add(rentalsPanel);
            
        } catch (IOException e) {
            System.err.println("Could not load rentals panel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onCartChanged() {
        updateCartBadge();
    }
    
    /**
     * Update the cart badge count
     */
    private void updateCartBadge() {
        int count = cartService.getItemCount();
        if (count > 0) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(true);
            cartBadge.setManaged(true);
        } else {
            cartBadge.setVisible(false);
            cartBadge.setManaged(false);
        }
    }
    
    /**
     * Toggle cart panel visibility
     */
    @FXML
    public void handleToggleCart(ActionEvent event) {
        toggleCart(!isCartOpen);
    }
    
    /**
     * Toggle cart panel with animation
     */
    public void toggleCart(boolean open) {
        if (open == isCartOpen) return;
        
        // Close rentals if open
        if (open && isRentalsOpen) {
            toggleRentals(false);
        }
        
        isCartOpen = open;
        
        if (open) {
            // Show overlay
            cartOverlay.setVisible(true);
            cartOverlay.setManaged(true);
            
            // Slide in animation
            cartPanelContainer.setTranslateX(350);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), cartPanelContainer);
            slideIn.setToX(0);
            slideIn.play();
            
            // Refresh cart contents
            if (cartPanelController != null) {
                cartPanelController.refreshCart();
            }
        } else {
            // Slide out animation
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), cartPanelContainer);
            slideOut.setToX(350);
            slideOut.setOnFinished(e -> {
                cartOverlay.setVisible(false);
                cartOverlay.setManaged(false);
            });
            slideOut.play();
        }
    }
    
    /**
     * Toggle rentals panel visibility
     */
    @FXML
    public void handleToggleRentals(ActionEvent event) {
        toggleRentals(!isRentalsOpen);
    }
    
    /**
     * Toggle rentals panel with animation
     */
    public void toggleRentals(boolean open) {
        if (open == isRentalsOpen) return;
        
        // Close cart if open
        if (open && isCartOpen) {
            toggleCart(false);
        }
        
        isRentalsOpen = open;
        
        if (open) {
            // Show overlay
            rentalsOverlay.setVisible(true);
            rentalsOverlay.setManaged(true);
            
            // Slide in animation
            rentalsPanelContainer.setTranslateX(380);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), rentalsPanelContainer);
            slideIn.setToX(0);
            slideIn.play();
            
            // Refresh rentals contents
            if (rentalsPanelController != null) {
                rentalsPanelController.loadRentals();
            }
        } else {
            // Slide out animation
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), rentalsPanelContainer);
            slideOut.setToX(380);
            slideOut.setOnFinished(e -> {
                rentalsOverlay.setVisible(false);
                rentalsOverlay.setManaged(false);
            });
            slideOut.play();
        }
    }
    
    /**
     * Navigate to the payment page
     */
    public void navigateToPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/GUI/views/PaymentView.fxml"));
            Parent view = loader.load();
            
            PaymentController paymentController = loader.getController();
            paymentController.setParentController(this);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().setAll(view);
            
            // Reset sidebar button selection
            resetAllButtonStyles();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load payment view: " + e.getMessage());
        }
    }

    private void resetAllButtonStyles() {
        // Reset all buttons to default state
        Button[] buttons = {btnAccueil, btnProfil, btnForum, btnEvenement, btnTechnicien, btnMarketplace, btnDeconnecter};
        Label[] labels = {lblAccueil, lblProfil, lblForum, lblEvenement, lblTechnicien, lblMarketplace, lblDeconnecter};
        SVGPath[] icons = {iconAccueil, iconProfil, iconForum, iconEvenement, iconTechnicien, iconMarketplace, iconDeconnecter};

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].getStyleClass().remove("sidebar-button-active");
            if (!buttons[i].getStyleClass().contains("sidebar-button")) {
                buttons[i].getStyleClass().add("sidebar-button");
            }
            buttons[i].setTranslateX(0); // Reset position
            if (labels[i] != null) {
                labels[i].setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
            }
            if (icons[i] != null) {
                icons[i].setFill(Color.web("#49ad32"));
            }
        }
    }

    private void setActiveButton(Button button, Label label, SVGPath icon) {
        resetAllButtonStyles();
        
        button.getStyleClass().remove("sidebar-button");
        button.getStyleClass().add("sidebar-button-active");
        
        // Animate button slide to the right
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), button);
        transition.setToX(5);
        transition.play();
        
        if (label != null) {
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        }
        if (icon != null) {
            icon.setFill(Color.web("#49ad32"));
        }
        
        currentActiveButton = button;
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentArea.getChildren().clear();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlPath);
            loadPlaceholder("Erreur de chargement");
        }
    }

    private void loadPlaceholder(String sectionName) {
        Label label = new Label(sectionName);
        label.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    @FXML
    void handleAccueil(ActionEvent event) {
        setActiveButton(btnAccueil, lblAccueil, iconAccueil);
        loadView("/marketplace/GUI/views/ClientAccueilView.fxml");
    }

    @FXML
    void handleProfil(ActionEvent event) {
        setActiveButton(btnProfil, lblProfil, iconProfil);
        loadPlaceholder("Mon Profil");
    }

    @FXML
    void handleForum(ActionEvent event) {
        setActiveButton(btnForum, lblForum, iconForum);
        loadPlaceholder("Forum");
    }

    @FXML
    void handleEvenement(ActionEvent event) {
        setActiveButton(btnEvenement, lblEvenement, iconEvenement);
        loadPlaceholder("Évènements");
    }

    @FXML
    void handleTechnicien(ActionEvent event) {
        setActiveButton(btnTechnicien, lblTechnicien, iconTechnicien);
        loadPlaceholder("Techniciens");
    }

    @FXML
    public void handleMarketplace(ActionEvent event) {
        setActiveButton(btnMarketplace, lblMarketplace, iconMarketplace);
        loadMarketplaceView();
    }
    
    private void loadMarketplaceView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/GUI/views/ClientMarketplaceView.fxml"));
            Parent view = loader.load();
            
            // Pass this controller to the marketplace controller for navigation
            ClientMarketplaceController marketplaceController = loader.getController();
            marketplaceController.setParentController(this);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load marketplace view");
            loadPlaceholder("Erreur de chargement");
        }
    }

    @FXML
    void handleDeconnecter(ActionEvent event) {
        try {
            Parent loginView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/marketplace/GUI/views/login.fxml")));
            contentArea.getScene().setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load login view");
        }
    }

    /**
     * Gets the content area for navigation purposes
     */
    public StackPane getContentArea() {
        return contentArea;
    }
}
