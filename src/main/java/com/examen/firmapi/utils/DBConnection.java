package com.examen.firmapi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static DBConnection databaseConnection;
    private Connection connection;

    private DBConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/firma-1", "root", "");
            System.out.println("Connexion etablie");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static DBConnection getInstance() {
        if (databaseConnection == null) {
            databaseConnection = new DBConnection();
        }
        return databaseConnection;
    }

    public Connection getConnection() {
        return connection;
    }
}
