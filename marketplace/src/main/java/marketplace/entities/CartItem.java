package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CartItem entity - Represents an item in the shopping cart
 * Supports both purchases (equipment) and rentals (vehicles/terrains)
 */
public class CartItem {
    
    public enum ItemType {
        PURCHASE,   // For equipment (achat)
        RENTAL      // For vehicles and terrains (location)
    }
    
    private int id;
    private Object product;          // Original entity (Equipement, Vehicule, or Terrain)
    private String productType;      // "Équipement", "Véhicule", "Terrain"
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;            // For purchases
    private LocalDate startDate;     // For rentals
    private LocalDate endDate;       // For rentals
    private int durationDays;        // For rentals
    private BigDecimal caution;      // For rentals
    private ItemType itemType;
    private String imageUrl;
    
    // Constructor for purchases (equipment)
    public CartItem(Object product, String productType, String productName, 
                    BigDecimal unitPrice, int quantity, String imageUrl) {
        this.id = System.identityHashCode(this);
        this.product = product;
        this.productType = productType;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.itemType = ItemType.PURCHASE;
        this.imageUrl = imageUrl;
        this.caution = BigDecimal.ZERO;
    }
    
    // Constructor for rentals (vehicles/terrains)
    public CartItem(Object product, String productType, String productName,
                    BigDecimal unitPrice, LocalDate startDate, LocalDate endDate,
                    BigDecimal caution, String imageUrl) {
        this.id = System.identityHashCode(this);
        this.product = product;
        this.productType = productType;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationDays = calculateDays(startDate, endDate);
        this.caution = caution != null ? caution : BigDecimal.ZERO;
        this.itemType = ItemType.RENTAL;
        this.imageUrl = imageUrl;
        this.quantity = 1;
    }
    
    private int calculateDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }
    
    /**
     * Calculate the subtotal for this item
     */
    public BigDecimal getSubtotal() {
        if (itemType == ItemType.PURCHASE) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            // For rental: price * duration
            return unitPrice.multiply(BigDecimal.valueOf(durationDays));
        }
    }
    
    /**
     * Get total with caution (for rentals)
     */
    public BigDecimal getTotalWithCaution() {
        return getSubtotal().add(caution);
    }
    
    /**
     * Get the product ID from the original entity
     */
    public int getProductId() {
        if (product instanceof Equipement) {
            return ((Equipement) product).getId();
        } else if (product instanceof Vehicule) {
            return ((Vehicule) product).getId();
        } else if (product instanceof Terrain) {
            return ((Terrain) product).getId();
        }
        return -1;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Object getProduct() { return product; }
    public void setProduct(Object product) { this.product = product; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate;
        if (endDate != null) {
            this.durationDays = calculateDays(startDate, endDate);
        }
    }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate;
        if (startDate != null) {
            this.durationDays = calculateDays(startDate, endDate);
        }
    }
    
    public int getDurationDays() { return durationDays; }
    
    public BigDecimal getCaution() { return caution; }
    public void setCaution(BigDecimal caution) { this.caution = caution; }
    
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    @Override
    public String toString() {
        if (itemType == ItemType.PURCHASE) {
            return String.format("CartItem[%s x%d = %.2f DT]", productName, quantity, getSubtotal());
        } else {
            return String.format("CartItem[%s, %d jours = %.2f DT + %.2f DT caution]", 
                productName, durationDays, getSubtotal(), caution);
        }
    }
}
