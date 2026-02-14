package marketplace.tools;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Singleton Database Connection Manager
 * Ensures only one database connection instance exists throughout the
 * application session
 */
public class DB_connection {
    // Singleton instance
    private static DB_connection instance;

    // Database connection
    private Connection con;

    // Database configuration
    private static final String DATABASE_NAME = "mp";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";
    private static final String DATABASE_URL = "jdbc:mysql://localhost/" + DATABASE_NAME;

    /**
     * Private constructor to prevent direct instantiation
     */
    private DB_connection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (Exception e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the singleton instance of DB_connection
     * 
     * @return The single instance of DB_connection
     */
    public static DB_connection getInstance() {
        if (instance == null) {
            synchronized (DB_connection.class) {
                if (instance == null) {
                    instance = new DB_connection();
                }
            }
        }
        return instance;
    }

    /**
     * Get the database connection
     * 
     * @return Active database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed or null, reconnect if needed
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                System.out.println("Database connection re-established.");
            }
        } catch (Exception e) {
            System.err.println("Error checking/re-establishing connection: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Database connection closed.");
            }
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
