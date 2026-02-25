package marketplace.service;

import marketplace.entities.*;
import marketplace.tools.DB_connection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Service for gathering statistics data for dashboard charts
 */
public class StatisticsService {
    
    private static StatisticsService instance;
    
    private StatisticsService() {}
    
    public static synchronized StatisticsService getInstance() {
        if (instance == null) {
            instance = new StatisticsService();
        }
        return instance;
    }
    
    // ============== EQUIPMENT STATISTICS ==============
    
    /**
     * Get equipment count by category (only equipment categories)
     */
    public Map<String, Integer> getEquipmentCountByCategory() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = """
            SELECT c.nom as categorie, COUNT(e.id) as count 
            FROM categories c 
            LEFT JOIN equipements e ON c.id = e.categorie_id 
            WHERE c.type_produit = 'equipement'
            GROUP BY c.id, c.nom 
            ORDER BY count DESC
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            data.put(rs.getString("categorie"), rs.getInt("count"));
        }
        
        return data;
    }
    
    /**
     * Get equipment stock status (available vs low stock vs out of stock)
     */
    public Map<String, Integer> getEquipmentStockStatus() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        
        String query = """
            SELECT 
                SUM(CASE WHEN quantite_stock > seuil_alerte THEN 1 ELSE 0 END) as en_stock,
                SUM(CASE WHEN quantite_stock <= seuil_alerte AND quantite_stock > 0 THEN 1 ELSE 0 END) as stock_faible,
                SUM(CASE WHEN quantite_stock = 0 THEN 1 ELSE 0 END) as rupture
            FROM equipements
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            data.put("En stock", rs.getInt("en_stock"));
            data.put("Stock faible", rs.getInt("stock_faible"));
            data.put("Rupture", rs.getInt("rupture"));
        }
        
        return data;
    }
    
    /**
     * Get total equipment value (stock * prix_vente)
     */
    public BigDecimal getTotalEquipmentValue() throws SQLException {
        String query = "SELECT SUM(quantite_stock * prix_vente) as total FROM equipements";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            BigDecimal result = rs.getBigDecimal("total");
            return result != null ? result : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
    
    // ============== ORDER STATISTICS ==============
    
    /**
     * Get orders count by payment status
     */
    public Map<String, Integer> getOrdersByPaymentStatus() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = """
            SELECT statut_paiement, COUNT(*) as count 
            FROM commandes 
            GROUP BY statut_paiement
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            String status = translatePaymentStatus(rs.getString("statut_paiement"));
            data.put(status, rs.getInt("count"));
        }
        
        return data;
    }
    
    /**
     * Get orders count by delivery status
     */
    public Map<String, Integer> getOrdersByDeliveryStatus() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = """
            SELECT statut_livraison, COUNT(*) as count 
            FROM commandes 
            GROUP BY statut_livraison
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            String status = translateDeliveryStatus(rs.getString("statut_livraison"));
            data.put(status, rs.getInt("count"));
        }
        
        return data;
    }
    
    /**
     * Get monthly revenue (last 6 months)
     */
    public Map<String, BigDecimal> getMonthlyRevenue() throws SQLException {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        String query = """
            SELECT 
                DATE_FORMAT(date_commande, '%Y-%m') as mois,
                SUM(montant_total) as revenue
            FROM commandes 
            WHERE statut_paiement = 'paye'
            AND date_commande >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
            GROUP BY DATE_FORMAT(date_commande, '%Y-%m')
            ORDER BY mois ASC
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        // Initialize last 6 months with 0
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            data.put(formatMonth(ym), BigDecimal.ZERO);
        }
        
        // Fill with actual data
        while (rs.next()) {
            String monthKey = rs.getString("mois");
            BigDecimal revenue = rs.getBigDecimal("revenue");
            // Convert to display format
            YearMonth ym = YearMonth.parse(monthKey);
            data.put(formatMonth(ym), revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        return data;
    }
    
    /**
     * Get total revenue
     */
    public BigDecimal getTotalRevenue() throws SQLException {
        String query = "SELECT SUM(montant_total) as total FROM commandes WHERE statut_paiement = 'paye'";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            BigDecimal result = rs.getBigDecimal("total");
            return result != null ? result : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
    
    // ============== RENTAL STATISTICS ==============
    
    /**
     * Get rentals by type (vehicule/terrain)
     */
    public Map<String, Integer> getRentalsByType() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = """
            SELECT type_location, COUNT(*) as count 
            FROM locations 
            GROUP BY type_location
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            String type = rs.getString("type_location");
            String displayType = "vehicule".equals(type) ? "Véhicules" : "Terrains";
            data.put(displayType, rs.getInt("count"));
        }
        
        return data;
    }
    
    /**
     * Get rentals by status
     */
    public Map<String, Integer> getRentalsByStatus() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = """
            SELECT statut, COUNT(*) as count 
            FROM locations 
            GROUP BY statut
            """;
        
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            String status = translateRentalStatus(rs.getString("statut"));
            data.put(status, rs.getInt("count"));
        }
        
        return data;
    }
    
    // ============== GLOBAL STATISTICS ==============
    
    /**
     * Get total counts for dashboard summary
     */
    public Map<String, Integer> getTotalCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        
        // Equipments
        String query1 = "SELECT COUNT(*) as count FROM equipements";
        PreparedStatement pst1 = DB_connection.getInstance().getConnection().prepareStatement(query1);
        ResultSet rs1 = pst1.executeQuery();
        if (rs1.next()) counts.put("equipements", rs1.getInt("count"));
        
        // Vehicles
        String query2 = "SELECT COUNT(*) as count FROM vehicules";
        PreparedStatement pst2 = DB_connection.getInstance().getConnection().prepareStatement(query2);
        ResultSet rs2 = pst2.executeQuery();
        if (rs2.next()) counts.put("vehicules", rs2.getInt("count"));
        
        // Terrains
        String query3 = "SELECT COUNT(*) as count FROM terrains";
        PreparedStatement pst3 = DB_connection.getInstance().getConnection().prepareStatement(query3);
        ResultSet rs3 = pst3.executeQuery();
        if (rs3.next()) counts.put("terrains", rs3.getInt("count"));
        
        // Suppliers
        String query4 = "SELECT COUNT(*) as count FROM fournisseurs";
        PreparedStatement pst4 = DB_connection.getInstance().getConnection().prepareStatement(query4);
        ResultSet rs4 = pst4.executeQuery();
        if (rs4.next()) counts.put("fournisseurs", rs4.getInt("count"));
        
        // Orders
        String query5 = "SELECT COUNT(*) as count FROM commandes";
        PreparedStatement pst5 = DB_connection.getInstance().getConnection().prepareStatement(query5);
        ResultSet rs5 = pst5.executeQuery();
        if (rs5.next()) counts.put("commandes", rs5.getInt("count"));
        
        // Rentals
        String query6 = "SELECT COUNT(*) as count FROM locations";
        PreparedStatement pst6 = DB_connection.getInstance().getConnection().prepareStatement(query6);
        ResultSet rs6 = pst6.executeQuery();
        if (rs6.next()) counts.put("locations", rs6.getInt("count"));
        
        return counts;
    }
    
    // ============== HELPER METHODS ==============
    
    private String translatePaymentStatus(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "en_attente": return "En attente";
            case "paye": return "Payé";
            case "rembourse": return "Remboursé";
            case "echoue": return "Échoué";
            default: return status;
        }
    }
    
    private String translateDeliveryStatus(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "en_preparation": return "En préparation";
            case "expedie": return "Expédié";
            case "en_livraison": return "En livraison";
            case "livre": return "Livré";
            case "annule": return "Annulé";
            default: return status;
        }
    }
    
    private String translateRentalStatus(String status) {
        if (status == null) return "Inconnu";
        switch (status) {
            case "en_attente": return "En attente";
            case "confirmee": return "Confirmée";
            case "en_cours": return "En cours";
            case "terminee": return "Terminée";
            case "annulee": return "Annulée";
            default: return status;
        }
    }
    
    private String formatMonth(YearMonth ym) {
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", 
                          "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        return months[ym.getMonthValue() - 1] + " " + ym.getYear();
    }
}
