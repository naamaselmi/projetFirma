package edu.connection3a7.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private String url="jdbc:mysql://localhost:3306/3a7";
    private String login="root";
    private String pwd="";
    public static MyConnection instance;
    private Connection cnx;
    public MyConnection() {
        try {
            cnx=DriverManager.getConnection(url,login,pwd);
            System.out.println("Connected établie...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance() {
        if(instance ==null){
            instance=new MyConnection();
        }
        return instance;
    }
}
