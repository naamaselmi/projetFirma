package edu.connection3a7.interfaces;


import edu.connection3a7.entities.Technicien;

import java.sql.SQLException;
import java.util.List;

public interface IService<T>{
    void addentitiy (T t) throws SQLException;
    void delet(T t) throws SQLException;
    void update (T t) throws SQLException;
    List<T> getdata() throws SQLException;

    List<Technicien> getData() throws SQLException;
}
