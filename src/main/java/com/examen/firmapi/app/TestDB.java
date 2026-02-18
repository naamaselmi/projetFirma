package com.examen.firmapi.app;

import com.examen.firmapi.utils.DBConnection;

import java.sql.Connection;

public class TestDB {

    public static void main(String[] args) {

        Connection conn = DBConnection
                .getInstance()
                .getConnection();

        if (conn != null) {
            System.out.println("✅ Test DB réussi : connexion active");
        } else {
            System.out.println("❌ Test DB échoué : connexion nulle");
        }
    }
}
