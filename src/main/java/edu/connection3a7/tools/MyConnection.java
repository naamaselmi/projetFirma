package edu.connection3a7.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url="jdbc:mysql://localhost:3306/firma";
    private String login="root";
    private String pwd="";
    public static  MyConnection instance;
    private Connection cnx;

    private MyConnection() {
        try {
            cnx=DriverManager.getConnection(url,login,pwd);
            System.out.println("connection established");
            DriverManager.getConnection(url,login,pwd);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }
    public static MyConnection getInstance() {
        if (instance==null){
            instance=new MyConnection();

        }
        return instance;
    }
}


