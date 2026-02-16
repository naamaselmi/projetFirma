package marketplace.GUI.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import marketplace.entities.CartItem;
import marketplace.service.CartService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Payment View
 * Handles order finalization and payment (Stripe-ready)
 */
public class PaymentController implements Initializable {
    
    @FXML private VBox orderItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label cautionsLabel;
    @FXML private Label totalLabel;
    @FXML private HBox cautionsRow;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField phoneField;
    @FXML private Button confirmPayButton;
    
    private CartService cartService;
    private Object parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartService = CartService.getInstance();
        loadOrderSummary();
    }
    
    /**
     * Set the parent controller for navigation
     */
    public void setParentController(Object controller) {
        this.parentController = controller;
    }
    
    /**
     * Load order summary from cart
     */
    private void loadOrderSummary() {
        orderItemsContainer.getChildren().clear();
        
        var items = cartService.getCartItems();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (CartItem item : items) {
            HBox itemRow = new HBox(15);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            
            // Item info
            VBox infoBox = new VBox(3);
            infoBox.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
            
            Label nameLabel = new Label(item.getProductName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
            nameLabel.setWrapText(true);
            
            String detailText;
            if (item.getItemType() == CartItem.ItemType.PURCHASE) {
                detailText = "Quantité: " + item.getQuantity() + " × " + 
                            String.format("%.2f DT", item.getUnitPrice());
            } else {
                detailText = item.getStartDate().format(dateFormat) + " - " + 
                            item.getEndDate().format(dateFormat) + 
                            " (" + item.getDurationDays() + " jours)";
            }
            
            Label detailLabel = new Label(detailText);
            detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            // Type badge
            String typeColor = getTypeColor(item.getProductType());
            Label typeLabel = new Label(item.getProductType());
            typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: " + 
                             typeColor + "; -fx-background-radius: 3; -fx-padding: 2 6;");
            
            infoBox.getChildren().addAll(nameLabel, detailLabel, typeLabel);
            
            // Price
            VBox priceBox = new VBox(2);
            priceBox.setAlignment(Pos.CENTER_RIGHT);
            
            Label priceLabel = new Label(String.format("%.2f DT", item.getSubtotal()));
            priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
            priceBox.getChildren().add(priceLabel);
            
            if (item.getItemType() == CartItem.ItemType.RENTAL && 
                item.getCaution().compareTo(BigDecimal.ZERO) > 0) {
                Label cautionLabel = new Label("+ " + String.format("%.2f DT", item.getCaution()) + " caution");
                cautionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #e67e22;");
                priceBox.getChildren().add(cautionLabel);
            }
            
            itemRow.getChildren().addAll(infoBox, priceBox);
            orderItemsContainer.getChildren().add(itemRow);
        }
        
        // Update totals
        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal cautions = cartService.getTotalCautions();
        BigDecimal total = cartService.getGrandTotal();
        
        subtotalLabel.setText(String.format("%.2f DT", subtotal));
        
        if (cautions.compareTo(BigDecimal.ZERO) > 0) {
            cautionsRow.setVisible(true);
            cautionsRow.setManaged(true);
            cautionsLabel.setText(String.format("%.2f DT", cautions));
        } else {
            cautionsRow.setVisible(false);
            cautionsRow.setManaged(false);
        }
        
        totalLabel.setText(String.format("%.2f DT", total));
    }
    
    private String getTypeColor(String type) {
        switch (type) {
            case "Équipement": return "#3498db";
            case "Véhicule": return "#e74c3c";
            case "Terrain": return "#27ae60";
            default: return "#888";
        }
    }
    
    @FXML
    private void handleBack() {
        // Navigate back to marketplace
        if (parentController instanceof ClientDashboardController) {
            ClientDashboardController dashboardController = (ClientDashboardController) parentController;
            dashboardController.handleMarketplace(new javafx.event.ActionEvent());
        }
    }
    
    @FXML
    private void handleConfirmPayment() {
        // Validate form
        if (addressField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer votre adresse de livraison.");
            addressField.requestFocus();
            return;
        }
        
        if (cityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer votre ville.");
            cityField.requestFocus();
            return;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer votre numéro de téléphone.");
            phoneField.requestFocus();
            return;
        }
        
        if (cartService.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide.");
            return;
        }
        
        // Confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la commande");
        confirm.setHeaderText("Finaliser votre commande");
        confirm.setContentText(String.format(
            "Montant total: %.2f DT\n\n" +
            "Adresse: %s, %s\n" +
            "Téléphone: %s\n\n" +
            "Voulez-vous confirmer cette commande?",
            cartService.getGrandTotal(),
            addressField.getText().trim(),
            cityField.getText().trim(),
            phoneField.getText().trim()
        ));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processOrder();
            }
        });
    }
    
    /**
     * Process the order - update stock, create records
     * In production, this would integrate with Stripe for payment
     */
    private void processOrder() {
        try {
            // Disable button during processing
            confirmPayButton.setDisable(true);
            confirmPayButton.setText("Traitement en cours...");
            
            // Process the cart (this updates stock and creates order records)
            boolean success = cartService.processCart();
            
            if (success) {
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Commande confirmée");
                successAlert.setHeaderText("Votre commande a été enregistrée!");
                successAlert.setContentText(
                    "Merci pour votre commande.\n\n" +
                    "Votre commande est en attente de traitement.\n" +
                    "Vous recevrez un email de confirmation avec les détails de paiement.\n\n" +
                    "Le paiement par Stripe sera disponible prochainement."
                );
                successAlert.showAndWait();
                
                // Navigate back to marketplace
                handleBack();
                
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors du traitement de votre commande.\nVeuillez réessayer.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error processing order: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur de base de données: " + e.getMessage());
        } finally {
            // Re-enable button
            confirmPayButton.setDisable(false);
            confirmPayButton.setText("Confirmer la commande");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
