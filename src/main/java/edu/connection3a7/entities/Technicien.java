package edu.connection3a7.entities;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Objects;

public class Technicien {

    private IntegerProperty id_tech;
    private ObjectProperty<Integer> id_utilisateur;
    private StringProperty nom;
    private StringProperty prenom;
    private StringProperty email;
    private StringProperty specialite;
    private StringProperty telephone;
    private BooleanProperty disponibilite;
    private StringProperty localisation;
    private StringProperty image;
    private StringProperty cin;
    private IntegerProperty age;
    private ObjectProperty<LocalDate> dateNaissance;

    public Technicien() {
        this.id_tech = new SimpleIntegerProperty();
        this.id_utilisateur = new SimpleObjectProperty<>();
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.specialite = new SimpleStringProperty();
        this.telephone = new SimpleStringProperty();
        this.disponibilite = new SimpleBooleanProperty();
        this.localisation = new SimpleStringProperty();
        this.image = new SimpleStringProperty();
        this.cin = new SimpleStringProperty();
        this.age = new SimpleIntegerProperty();
        this.dateNaissance = new SimpleObjectProperty<>();
    }

    public Technicien(Integer id_tech, Integer id_utilisateur, String nom, String prenom,
                      String email, String specialite, String telephone,
                      boolean disponibilite, String localisation, String image,
                      String cin, int age, LocalDate dateNaissance) {
        this();
        this.id_tech.set(id_tech);
        this.id_utilisateur.set(id_utilisateur);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.email.set(email);
        this.specialite.set(specialite);
        this.telephone.set(telephone);
        this.disponibilite.set(disponibilite);
        this.localisation.set(localisation);
        this.image.set(image);
        this.cin.set(cin);
        this.age.set(age);
        this.dateNaissance.set(dateNaissance);
    }

    // ====== Getters & Setters ======
    public int getId_tech() { return id_tech.get(); }
    public void setId_tech(int id) { this.id_tech.set(id); }
    public IntegerProperty idTechProperty() { return id_tech; }

    public Integer getId_utilisateur() { return id_utilisateur.get(); }
    public void setId_utilisateur(Integer id) { this.id_utilisateur.set(id); }
    public ObjectProperty<Integer> idUtilisateurProperty() { return id_utilisateur; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getSpecialite() { return specialite.get(); }
    public void setSpecialite(String specialite) { this.specialite.set(specialite); }
    public StringProperty specialiteProperty() { return specialite; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }

    public boolean isDisponibilite() { return disponibilite.get(); }
    public void setDisponibilite(boolean dispo) { this.disponibilite.set(dispo); }
    public BooleanProperty disponibiliteProperty() { return disponibilite; }

    public String getLocalisation() { return localisation.get(); }
    public void setLocalisation(String localisation) { this.localisation.set(localisation); }
    public StringProperty localisationProperty() { return localisation; }

    public String getImage() { return image.get(); }
    public void setImage(String image) { this.image.set(image); }
    public StringProperty imageProperty() { return image; }

    public String getCin() { return cin.get(); }
    public void setCin(String cin) { this.cin.set(cin); }
    public StringProperty cinProperty() { return cin; }

    public int getAge() { return age.get(); }
    public void setAge(int age) { this.age.set(age); }
    public IntegerProperty ageProperty() { return age; }

    public LocalDate getDateNaissance() { return dateNaissance.get(); }
    public void setDateNaissance(LocalDate date) { this.dateNaissance.set(date); }
    public ObjectProperty<LocalDate> dateNaissanceProperty() { return dateNaissance; }

    // ===== EQUALS and HASHCODE =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Technicien that = (Technicien) o;
        return Objects.equals(getId_tech(), that.getId_tech());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_tech());
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "Technicien{" +
                "id_tech=" + getId_tech() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", specialite='" + getSpecialite() + '\'' +
                ", disponibilite=" + isDisponibilite() +
                '}';
    }
}