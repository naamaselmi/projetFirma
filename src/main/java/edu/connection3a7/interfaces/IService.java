package edu.connection3a7.interfaces;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public interface IService <T>
{
    void addEntity(T t) throws SQLException;
    void deleteEntity(T t) throws SQLException;
    void updateEntity(int id, T t) throws SQLException;
    List<T> getData () throws Exception;
}
