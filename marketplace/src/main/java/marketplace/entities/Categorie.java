package marketplace.entities;

import java.util.Objects;

/**
 * Categorie entity - Maps to categories table
 * Represents product categories for equipment, vehicles, and land
 */
public class Categorie {
    private int id;
    private String nom;
    private ProductType typeProduit;
    private String description;

    // Constructors
    public Categorie() {
    }

    public Categorie(String nom, ProductType typeProduit, String description) {
        this.nom = nom;
        this.typeProduit = typeProduit;
        this.description = description;
    }

    public Categorie(int id, String nom, ProductType typeProduit, String description) {
        this.id = id;
        this.nom = nom;
        this.typeProduit = typeProduit;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ProductType getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(ProductType typeProduit) {
        this.typeProduit = typeProduit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Categorie categorie = (Categorie) o;
        return id == categorie.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
