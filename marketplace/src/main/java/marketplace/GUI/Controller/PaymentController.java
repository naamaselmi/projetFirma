package marketplace.GUI.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import marketplace.entities.CartItem;
import marketplace.service.CartService;
import marketplace.service.StripeService;
import marketplace.tools.MapPicker;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Payment View
 * Handles order finalization and Stripe payment processing
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
    
    // Card payment fields
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvcField;
    @FXML private TextField cardHolderField;
    @FXML private Label cardBrandLabel;
    
    private CartService cartService;
    private StripeService stripeService;
    private Object parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartService = CartService.getInstance();
        stripeService = StripeService.getInstance();
        
        loadOrderSummary();
        setupCardFieldListeners();
    }
    
    /**
     * Setup listeners for card fields (formatting and validation)
     */
    private void setupCardFieldListeners() {
        // Card number formatting and brand detection
        if (cardNumberField != null) {
            cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                
                // Remove non-digits
                String digits = newVal.replaceAll("[^\\d]", "");
                
                // Limit to 16 digits
                if (digits.length() > 16) {
                    digits = digits.substring(0, 16);
                }
                
                // Format with spaces every 4 digits
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digits.charAt(i));
                }
                
                String formattedStr = formatted.toString();
                if (!formattedStr.equals(newVal)) {
                    cardNumberField.setText(formattedStr);
                    cardNumberField.positionCaret(formattedStr.length());
                }
                
                // Update card brand
                if (cardBrandLabel != null) {
                    String brand = stripeService.getCardBrand(digits);
                    cardBrandLabel.setText(brand);
                }
            });
        }
        
        // Expiry date formatting (MM/YY)
        if (expiryField != null) {
            expiryField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                
                String digits = newVal.replaceAll("[^\\d]", "");
                
                // Limit to 4 digits (MMYY)
                if (digits.length() > 4) {
                    digits = digits.substring(0, 4);
                }
                
                // Add slash after month
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2) {
                        formatted.append("/");
                    }
                    formatted.append(digits.charAt(i));
                }
                
                String formattedStr = formatted.toString();
                if (!formattedStr.equals(newVal)) {
                    expiryField.setText(formattedStr);
                    expiryField.positionCaret(formattedStr.length());
                }
            });
        }
        
        // CVC - digits only, max 4
        if (cvcField != null) {
            cvcField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;
                
                String digits = newVal.replaceAll("[^\\d]", "");
                if (digits.length() > 4) {
                    digits = digits.substring(0, 4);
                }
                
                if (!digits.equals(newVal)) {
                    cvcField.setText(digits);
                    cvcField.positionCaret(digits.length());
                }
            });
        }
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
        // Validate shipping form
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
        
        // Validate card fields
        if (cardNumberField == null || cardNumberField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer le numéro de carte.");
            if (cardNumberField != null) cardNumberField.requestFocus();
            return;
        }
        
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        if (!stripeService.isValidCardNumber(cardNumber)) {
            showAlert(Alert.AlertType.WARNING, "Carte invalide", "Le numéro de carte est invalide.");
            cardNumberField.requestFocus();
            return;
        }
        
        if (expiryField == null || expiryField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer la date d'expiration.");
            if (expiryField != null) expiryField.requestFocus();
            return;
        }
        
        // Parse expiry date (MM/YY)
        String[] expiryParts = expiryField.getText().split("/");
        if (expiryParts.length != 2) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "Format de date invalide (MM/YY).");
            expiryField.requestFocus();
            return;
        }
        
        int expMonth, expYear;
        try {
            expMonth = Integer.parseInt(expiryParts[0].trim());
            expYear = 2000 + Integer.parseInt(expiryParts[1].trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "Format de date invalide (MM/YY).");
            expiryField.requestFocus();
            return;
        }
        
        if (!stripeService.isValidExpiry(expMonth, expYear)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date d'expiration est invalide.");
            expiryField.requestFocus();
            return;
        }
        
        if (cvcField == null || cvcField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ requis", "Veuillez entrer le code CVC.");
            if (cvcField != null) cvcField.requestFocus();
            return;
        }
        
        String cvc = cvcField.getText().trim();
        if (!stripeService.isValidCVC(cvc)) {
            showAlert(Alert.AlertType.WARNING, "CVC invalide", "Le code CVC est invalide.");
            cvcField.requestFocus();
            return;
        }
        
        if (cartService.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide.");
            return;
        }
        
        // Get card holder name (optional but recommended)
        String cardHolder = "";
        if (cardHolderField != null) {
            cardHolder = cardHolderField.getText().trim();
        }
        
        // Confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le paiement");
        confirm.setHeaderText("Finaliser votre paiement par carte");
        confirm.setContentText(String.format(
            "Montant à débiter: %.2f DT\n\n" +
            "Carte: •••• •••• •••• %s (%s)\n" +
            "Adresse: %s, %s\n" +
            "Téléphone: %s\n\n" +
            "Voulez-vous confirmer ce paiement?",
            cartService.getGrandTotal(),
            cardNumber.substring(Math.max(0, cardNumber.length() - 4)),
            stripeService.getCardBrand(cardNumber),
            addressField.getText().trim(),
            cityField.getText().trim(),
            phoneField.getText().trim()
        ));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processStripePayment(cardNumber, expMonth, expYear, cvc);
            }
        });
    }
    
    /**
     * Process the payment with Stripe and then create the order
     */
    private void processStripePayment(String cardNumber, int expMonth, int expYear, String cvc) {
        // Disable button during processing
        confirmPayButton.setDisable(true);
        confirmPayButton.setText("Paiement en cours...");
        
        BigDecimal amount = cartService.getGrandTotal();
        String description = "Commande Marketplace - " + cartService.getItemCount() + " article(s)";
        
        // Process payment in background thread
        CompletableFuture.supplyAsync(() -> {
            return stripeService.processPayment(cardNumber, expMonth, expYear, cvc, amount, description, null);
        }).thenAccept(result -> {
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    // Payment successful, now create the order
                    processOrderAfterPayment(result.getPaymentIntentId());
                } else {
                    // Payment failed
                    showAlert(Alert.AlertType.ERROR, "Échec du paiement", result.getMessage());
                    confirmPayButton.setDisable(false);
                    confirmPayButton.setText("Payer maintenant");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de paiement: " + e.getMessage());
                confirmPayButton.setDisable(false);
                confirmPayButton.setText("Payer maintenant");
            });
            return null;
        });
    }
    
    /**
     * Process the order after successful Stripe payment
     */
    private void processOrderAfterPayment(String paymentIntentId) {
        try {
            // Process the cart (this updates stock and creates order records)
            boolean success = cartService.processCart();
            
            if (success) {
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Paiement réussi!");
                successAlert.setHeaderText("Votre paiement a été effectué avec succès!");
                successAlert.setContentText(
                    "Merci pour votre commande.\n\n" +
                    "Référence de paiement: " + paymentIntentId.substring(0, Math.min(20, paymentIntentId.length())) + "...\n\n" +
                    "Votre commande est en cours de traitement.\n" +
                    "Vous recevrez un email de confirmation sous peu."
                );
                successAlert.showAndWait();
                
                // Navigate back to marketplace
                handleBack();
                
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Le paiement a réussi mais une erreur est survenue lors de la création de la commande.\n" +
                    "Veuillez contacter le support avec la référence: " + paymentIntentId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error processing order: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Le paiement a réussi mais une erreur de base de données est survenue.\n" +
                "Référence de paiement: " + paymentIntentId + "\n" +
                "Erreur: " + e.getMessage());
        } finally {
            // Re-enable button
            confirmPayButton.setDisable(false);
            confirmPayButton.setText("Payer maintenant");
        }
    }
    
    /**
     * Legacy processOrder method - kept for backward compatibility
     * Process the order - update stock, create records
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
    
    @FXML
    private void handleOpenMap(javafx.event.ActionEvent event) {
        MapPicker mapPicker = new MapPicker();
        MapPicker.AddressResult result = mapPicker.showAndWait(
            (Stage) cityField.getScene().getWindow(),
            addressField.getText(),
            cityField.getText()
        );
        
        if (result.isConfirmed()) {
            addressField.setText(result.getAddress());
            cityField.setText(result.getCity());
        }
    }
}
