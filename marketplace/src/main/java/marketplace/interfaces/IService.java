package marketplace.interfaces;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic service interface for CRUD operations
 * 
 * @param <T> Entity type
 */
public interface IService<T> {

    /**
     * Add a new entity to the database
     * 
     * @param entity Entity to add
     * @throws SQLException if database error occurs
     */
    void addEntity(T entity) throws SQLException;

    /**
     * Delete an entity from the database
     * 
     * @param entity Entity to delete
     * @throws SQLException if database error occurs
     */
    void deleteEntity(T entity) throws SQLException;

    /**
     * Update an existing entity
     * 
     * @param entity Entity with updated data
     * @throws SQLException if database error occurs
     */
    void updateEntity(T entity) throws SQLException;

    /**
     * Get all entities from the database
     * 
     * @return List of all entities
     * @throws SQLException if database error occurs
     */
    List<T> getEntities() throws SQLException;
}
