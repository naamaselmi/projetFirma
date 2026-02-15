package edu.connection3a7.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DetailPersonne {

    @FXML
    private TextField nom;

    @FXML
    private TextField prenom;

    public TextField getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom.setText(nom);
    }

    public TextField getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom.setText(prenom);
    }
}
