package Firma.tools.GestionMarketplace;

import Firma.tools.GestionEvenement.MyConnection;
import java.sql.Connection;

/**
 * Database connection adapter for Marketplace services.
 * Delegates to MyConnection (GestionEvenement) so both modules
 * share the same single database connection.
 */
public class DB_connection {
    private static DB_connection instance;

    private DB_connection() {
    }

    public static synchronized DB_connection getInstance() {
        if (instance == null) {
            instance = new DB_connection();
        }
        return instance;
    }

    public Connection getConnection() {
        return MyConnection.getInstance().getCnx();
    }
}
