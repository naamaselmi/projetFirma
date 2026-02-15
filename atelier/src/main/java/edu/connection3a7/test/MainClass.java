package edu.connection3a7.test;

import edu.connection3a7.entities.Personne;
import edu.connection3a7.services.PersonneService;
import edu.connection3a7.tools.MyConnection;

import java.sql.SQLException;

public class MainClass {
    public static void main(String[] args) {
        //MyConnection mc = new MyConnection();
        Personne p1 = new Personne("slimani","hamza");
        PersonneService ps = new PersonneService();
        /*try {
            ps.addEntity2(p1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
        try {
            System.out.println(ps.getData());
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }
}
