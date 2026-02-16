package marketplace.GUI.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import marketplace.entities.CartItem;
import marketplace.entities.Equipement;
import marketplace.service.CartService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Cart Panel
 * Displays cart items and handles cart operations
 */
public class CartPanelController implements Initializable, CartService.CartChangeListener {
    
    @FXML private VBox cartPanel;
    @FXML private VBox cartItemsContainer;
    @FXML private Label itemCountLabel;
    @FXML private Label emptyCartLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label cautionsLabel;
    @FXML private Label totalLabel;
    @FXML private HBox cautionsRow;
    @FXML private Button payButton;
    @FXML private Button clearButton;
    @FXML private Button closeButton;
    
    private CartService cartService;
    private Runnable onCloseCallback;
    private Runnable onPayCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartService = CartService.getInstance();
        cartService.addCartChangeListener(this);
        refreshCart();
    }
    
    /**
     * Set callback for close action
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
    
    /**
     * Set callback for pay action
     */
    public void setOnPayCallback(Runnable callback) {
        this.onPayCallback = callback;
    }
    
    @Override
    public void onCartChanged() {
        refreshCart();
    }
    
    /**
     * Refresh the cart display
     */
    public void refreshCart() {
        cartItemsContainer.getChildren().clear();
        
        var items = cartService.getCartItems();
        
        if (items.isEmpty()) {
            emptyCartLabel.setVisible(true);
            emptyCartLabel.setManaged(true);
            cartItemsContainer.getChildren().add(emptyCartLabel);
            payButton.setDisable(true);
            clearButton.setDisable(true);
        } else {
            emptyCartLabel.setVisible(false);
            emptyCartLabel.setManaged(false);
            payButton.setDisable(false);
            clearButton.setDisable(false);
            
            for (CartItem item : items) {
                VBox itemCard = createCartItemCard(item);
                cartItemsContainer.getChildren().add(itemCard);
            }
        }
        
        // Update counts and totals
        int count = cartService.getItemCount();
        itemCountLabel.setText(count + " article" + (count > 1 ? "s" : "") + " dans le panier");
        
        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal cautions = cartService.getTotalCautions();
        BigDecimal total = cartService.getGrandTotal();
        
        subtotalLabel.setText(String.format("%.2f DT", subtotal));
        
        // Show cautions row if there are rentals
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
    
    /**
     * Create a card UI for a cart item
     */
    private VBox createCartItemCard(CartItem item) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-padding: 12;");
        
        // Top row: Image and info
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Product image placeholder
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefSize(60, 60);
        imageBox.setMinSize(60, 60);
        imageBox.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 6;");
        
        // Try to load image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(55);
        imageView.setFitHeight(55);
        imageView.setPreserveRatio(true);
        
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                URL resourceUrl = getClass().getResource("/image/" + item.getImageUrl());
                if (resourceUrl != null) {
                    imageView.setImage(new Image(resourceUrl.toExternalForm()));
                    imageBox.getChildren().add(imageView);
                }
            } catch (Exception e) {
                // Use placeholder icon
                addPlaceholderIcon(imageBox, item.getProductType());
            }
        } else {
            addPlaceholderIcon(imageBox, item.getProductType());
        }
        
        // Product info
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        
        Label typeLabel = new Label(item.getProductType());
        String typeColor = getTypeColor(item.getProductType());
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + typeColor + "; -fx-font-weight: bold;");
        
        infoBox.getChildren().addAll(nameLabel, typeLabel);
        
        // Delete button
        Button deleteBtn = new Button();
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        deleteIcon.setFill(javafx.scene.paint.Color.web("#e74c3c"));
        deleteIcon.setScaleX(0.9);
        deleteIcon.setScaleY(0.9);
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setOnAction(e -> {
            cartService.removeItem(item);
        });
        
        topRow.getChildren().addAll(imageBox, infoBox, deleteBtn);
        
        // Bottom row: Quantity/Duration and Price
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        if (item.getItemType() == CartItem.ItemType.PURCHASE) {
            // Quantity controls for purchases
            HBox quantityBox = new HBox(5);
            quantityBox.setAlignment(Pos.CENTER_LEFT);
            
            Button minusBtn = new Button("-");
            minusBtn.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 4; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");
            
            Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
            qtyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center;");
            qtyLabel.setAlignment(Pos.CENTER);
            
            Button plusBtn = new Button("+");
            plusBtn.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 4; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");
            
            // Get max stock
            int maxStock = item.getProduct() instanceof Equipement ? 
                ((Equipement) item.getProduct()).getQuantiteStock() : 99;
            
            minusBtn.setOnAction(e -> {
                if (item.getQuantity() > 1) {
                    cartService.updateQuantity(item, item.getQuantity() - 1);
                }
            });
            
            plusBtn.setOnAction(e -> {
                if (item.getQuantity() < maxStock) {
                    cartService.updateQuantity(item, item.getQuantity() + 1);
                }
            });
            
            quantityBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            bottomRow.getChildren().add(quantityBox);
            
        } else {
            // Duration info for rentals
            VBox durationBox = new VBox(2);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            Label datesLabel = new Label(
                item.getStartDate().format(fmt) + " - " + item.getEndDate().format(fmt)
            );
            datesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            
            Label durationLabel = new Label(item.getDurationDays() + " jour(s)");
            durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
            
            durationBox.getChildren().addAll(datesLabel, durationLabel);
            bottomRow.getChildren().add(durationBox);
        }
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        bottomRow.getChildren().add(spacer);
        
        // Price
        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label priceLabel = new Label(String.format("%.2f DT", item.getSubtotal()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        
        priceBox.getChildren().add(priceLabel);
        
        // Show caution for rentals
        if (item.getItemType() == CartItem.ItemType.RENTAL && item.getCaution().compareTo(BigDecimal.ZERO) > 0) {
            Label cautionLabel = new Label("+ " + String.format("%.2f DT", item.getCaution()) + " caution");
            cautionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #e67e22;");
            priceBox.getChildren().add(cautionLabel);
        }
        
        bottomRow.getChildren().add(priceBox);
        
        card.getChildren().addAll(topRow, bottomRow);
        
        return card;
    }
    
    private void addPlaceholderIcon(VBox container, String type) {
        SVGPath icon = new SVGPath();
        switch (type) {
            case "Équipement":
                icon.setContent("M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z");
                break;
            case "Véhicule":
                icon.setContent("M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99z");
                break;
            case "Terrain":
                icon.setContent("M12 3L2 12h3v8h14v-8h3L12 3zm0 12.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z");
                break;
            default:
                icon.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z");
        }
        icon.setFill(javafx.scene.paint.Color.web("#999"));
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        container.getChildren().add(icon);
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
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
    
    @FXML
    private void handlePay() {
        if (cartService.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Ajoutez des articles au panier avant de procéder au paiement.");
            return;
        }
        
        if (onPayCallback != null) {
            onPayCallback.run();
        }
    }
    
    @FXML
    private void handleClearCart() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider le panier");
        confirm.setHeaderText("Confirmer");
        confirm.setContentText("Êtes-vous sûr de vouloir vider votre panier ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cartService.clearCart();
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
