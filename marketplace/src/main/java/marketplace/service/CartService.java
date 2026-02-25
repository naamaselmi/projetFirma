package marketplace.service;

import marketplace.entities.*;
import marketplace.tools.DB_connection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CartService - Singleton service for managing the shopping cart
 * Handles both purchases (equipment) and rentals (vehicles/terrains)
 */
public class CartService {

    private static CartService instance;

    private List<CartItem> cartItems;
    private int currentUserId;
    private List<CartChangeListener> listeners;

    // Interface for cart change notifications
    public interface CartChangeListener {
        void onCartChanged();
    }

    private CartService() {
        this.cartItems = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.currentUserId = -1;
    }

    /**
     * Get the singleton instance
     */
    public static synchronized CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    /**
     * Set the current user
     */
    public void setCurrentUser(int userId) {
        this.currentUserId = userId;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Add a listener for cart changes
     */
    public void addCartChangeListener(CartChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeCartChangeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of cart changes
     */
    private void notifyListeners() {
        for (CartChangeListener listener : listeners) {
            try {
                listener.onCartChanged();
            } catch (Exception e) {
                System.err.println("Error notifying cart listener: " + e.getMessage());
            }
        }
    }

    /**
     * Add equipment to cart (purchase)
     */
    public boolean addEquipment(Equipement equipement, int quantity) {
        if (equipement == null || quantity <= 0) {
            return false;
        }

        // Check available stock
        if (quantity > equipement.getQuantiteStock()) {
            System.err.println("Quantité demandée supérieure au stock disponible");
            return false;
        }

        // Check if item already in cart
        Optional<CartItem> existing = findEquipmentInCart(equipement.getId());
        if (existing.isPresent()) {
            // Update quantity
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > equipement.getQuantiteStock()) {
                System.err.println("Quantité totale supérieure au stock disponible");
                return false;
            }
            item.setQuantity(newQuantity);
        } else {
            // Add new item
            CartItem item = new CartItem(
                    equipement,
                    "Équipement",
                    equipement.getNom(),
                    equipement.getPrixVente(),
                    quantity,
                    equipement.getImageUrl());
            cartItems.add(item);
        }

        notifyListeners();
        return true;
    }

    /**
     * Add vehicle rental to cart
     */
    public boolean addVehicleRental(Vehicule vehicule, LocalDate startDate, LocalDate endDate) {
        if (vehicule == null || startDate == null || endDate == null) {
            return false;
        }

        if (endDate.isBefore(startDate)) {
            System.err.println("Date de fin avant la date de début");
            return false;
        }

        // Check if vehicle is already in cart
        if (isVehicleInCart(vehicule.getId())) {
            System.err.println("Ce véhicule est déjà dans le panier");
            return false;
        }

        // Check vehicle availability
        if (!vehicule.isDisponible()) {
            System.err.println("Ce véhicule n'est pas disponible");
            return false;
        }

        CartItem item = new CartItem(
                vehicule,
                "Véhicule",
                vehicule.getNom(),
                vehicule.getPrixJour(),
                startDate,
                endDate,
                vehicule.getCaution(),
                vehicule.getImageUrl());
        cartItems.add(item);

        notifyListeners();
        return true;
    }

    /**
     * Add terrain rental to cart
     */
    public boolean addTerrainRental(Terrain terrain, LocalDate startDate, LocalDate endDate) {
        if (terrain == null || startDate == null || endDate == null) {
            return false;
        }

        if (endDate.isBefore(startDate)) {
            System.err.println("Date de fin avant la date de début");
            return false;
        }

        // Check if terrain is already in cart
        if (isTerrainInCart(terrain.getId())) {
            System.err.println("Ce terrain est déjà dans le panier");
            return false;
        }

        // Check terrain availability
        if (!terrain.isDisponible()) {
            System.err.println("Ce terrain n'est pas disponible");
            return false;
        }

        // Calculate daily price from monthly price
        BigDecimal dailyPrice = terrain.getPrixMois() != null
                ? terrain.getPrixMois().divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP)
                : terrain.getPrixAnnee().divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP);

        CartItem item = new CartItem(
                terrain,
                "Terrain",
                terrain.getTitre(),
                dailyPrice,
                startDate,
                endDate,
                terrain.getCaution(),
                terrain.getImageUrl());
        cartItems.add(item);

        notifyListeners();
        return true;
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        cartItems.remove(item);
        notifyListeners();
    }

    /**
     * Remove item by index
     */
    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
            notifyListeners();
        }
    }

    /**
     * Update equipment quantity
     */
    public boolean updateQuantity(CartItem item, int newQuantity) {
        if (item.getItemType() != CartItem.ItemType.PURCHASE) {
            return false;
        }

        if (newQuantity <= 0) {
            removeItem(item);
            return true;
        }

        // Check stock
        if (item.getProduct() instanceof Equipement) {
            Equipement eq = (Equipement) item.getProduct();
            if (newQuantity > eq.getQuantiteStock()) {
                return false;
            }
        }

        item.setQuantity(newQuantity);
        notifyListeners();
        return true;
    }

    /**
     * Update rental dates
     */
    public boolean updateRentalDates(CartItem item, LocalDate startDate, LocalDate endDate) {
        if (item.getItemType() != CartItem.ItemType.RENTAL) {
            return false;
        }

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return false;
        }

        item.setStartDate(startDate);
        item.setEndDate(endDate);
        notifyListeners();
        return true;
    }

    /**
     * Clear the entire cart
     */
    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    /**
     * Get all items in cart
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    /**
     * Get number of items in cart
     */
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Get total quantity (for purchases)
     */
    public int getTotalQuantity() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Calculate subtotal (without cautions)
     */
    public BigDecimal getSubtotal() {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total cautions
     */
    public BigDecimal getTotalCautions() {
        return cartItems.stream()
                .filter(item -> item.getItemType() == CartItem.ItemType.RENTAL)
                .map(CartItem::getCaution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate grand total (subtotal + cautions)
     */
    public BigDecimal getGrandTotal() {
        return getSubtotal().add(getTotalCautions());
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Find equipment in cart by ID
     */
    private Optional<CartItem> findEquipmentInCart(int equipementId) {
        return cartItems.stream()
                .filter(item -> item.getProductType().equals("Équipement"))
                .filter(item -> item.getProduct() instanceof Equipement)
                .filter(item -> ((Equipement) item.getProduct()).getId() == equipementId)
                .findFirst();
    }

    /**
     * Check if vehicle is in cart
     */
    private boolean isVehicleInCart(int vehiculeId) {
        return cartItems.stream()
                .filter(item -> item.getProductType().equals("Véhicule"))
                .filter(item -> item.getProduct() instanceof Vehicule)
                .anyMatch(item -> ((Vehicule) item.getProduct()).getId() == vehiculeId);
    }

    /**
     * Check if terrain is in cart
     */
    private boolean isTerrainInCart(int terrainId) {
        return cartItems.stream()
                .filter(item -> item.getProductType().equals("Terrain"))
                .filter(item -> item.getProduct() instanceof Terrain)
                .anyMatch(item -> ((Terrain) item.getProduct()).getId() == terrainId);
    }

    /**
     * Get items for purchase (equipment only)
     */
    public List<CartItem> getPurchaseItems() {
        return cartItems.stream()
                .filter(item -> item.getItemType() == CartItem.ItemType.PURCHASE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get items for rental (vehicles and terrains)
     */
    public List<CartItem> getRentalItems() {
        return cartItems.stream()
                .filter(item -> item.getItemType() == CartItem.ItemType.RENTAL)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Process the cart - finalize purchases and rentals
     * This updates stock and availability in the database
     * 
     * @return the created Commande if successful (with its DB id), or null on
     *         failure
     */
    public Commande processCart() throws SQLException {
        if (isEmpty()) {
            return null;
        }

        if (currentUserId <= 0) {
            System.err.println("Utilisateur non connecté");
            return null;
        }

        EquipementService equipementService = new EquipementService();
        VehiculeService vehiculeService = new VehiculeService();
        TerrainService terrainService = new TerrainService();
        LocationService locationService = new LocationService();
        CommandeService commandeService = new CommandeService();
        DetailCommandeService detailCommandeService = new DetailCommandeService();

        Commande createdCommande = null;
        try {
            // Process purchases (create commande)
            List<CartItem> purchases = getPurchaseItems();
            if (!purchases.isEmpty()) {
                // Create new commande
                Commande commande = new Commande();
                commande.setUtilisateurId(currentUserId);
                commande.setMontantTotal(purchases.stream()
                        .map(CartItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                commande.setStatutPaiement(PaymentStatus.PAYE);
                commande.setStatutLivraison(DeliveryStatus.EN_ATTENTE);
                commande.setAdresseLivraison("À confirmer"); // Will be set during payment

                commandeService.addEntity(commande); // DB id is now set on commande
                createdCommande = commande;

                for (CartItem item : purchases) {
                    if (item.getProduct() instanceof Equipement) {
                        Equipement eq = (Equipement) item.getProduct();
                        int newStock = eq.getQuantiteStock() - item.getQuantity();
                        if (newStock < 0) {
                            throw new SQLException("Stock insuffisant pour: " + eq.getNom());
                        }
                        eq.setQuantiteStock(newStock);

                        // Update availability if stock is 0
                        if (newStock == 0) {
                            eq.setDisponible(false);
                        }

                        equipementService.updateEntity(eq);

                        // Insert DetailCommande row for this item
                        DetailCommande detail = new DetailCommande(
                                commande.getId(),
                                eq.getId(),
                                item.getQuantity(),
                                item.getUnitPrice());
                        detailCommandeService.addEntity(detail);
                    }
                }
            }

            // Process rentals (create locations)
            List<CartItem> rentals = getRentalItems();
            for (CartItem item : rentals) {
                Location location = new Location();
                location.setUtilisateurId(currentUserId);
                location.setDateDebut(item.getStartDate());
                location.setDateFin(item.getEndDate());
                location.setDureeJours(item.getDurationDays());
                location.setPrixTotal(item.getSubtotal());
                location.setCaution(item.getCaution());
                location.setStatut(RentalStatus.EN_ATTENTE);

                if (item.getProduct() instanceof Vehicule) {
                    Vehicule v = (Vehicule) item.getProduct();
                    location.setTypeLocation("vehicule");
                    location.setElementId(v.getId());

                    // Mark vehicle as unavailable
                    v.setDisponible(false);
                    vehiculeService.updateEntity(v);

                } else if (item.getProduct() instanceof Terrain) {
                    Terrain t = (Terrain) item.getProduct();
                    location.setTypeLocation("terrain");
                    location.setElementId(t.getId());

                    // Mark terrain as unavailable
                    t.setDisponible(false);
                    terrainService.updateEntity(t);
                }

                locationService.addEntity(location);
            }

            // Clear cart after successful processing
            clearCart();
            return createdCommande;

        } catch (SQLException e) {
            System.err.println("Erreur lors du traitement du panier: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get available stock for an equipment (considering cart items)
     */
    public int getAvailableStock(Equipement equipement) {
        int inCart = cartItems.stream()
                .filter(item -> item.getProduct() instanceof Equipement)
                .filter(item -> ((Equipement) item.getProduct()).getId() == equipement.getId())
                .mapToInt(CartItem::getQuantity)
                .sum();

        return equipement.getQuantiteStock() - inCart;
    }
}
