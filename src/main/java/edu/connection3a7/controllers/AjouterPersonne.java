package edu.connection3a7.controllers;

import edu.connection3a7.entities.Personne;
import edu.connection3a7.services.PersonneService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterPersonne {

    @FXML
    private TextField nomtextfield;

    @FXML
    private TextField prenomtextfield;
    private Personne personne;
    private PersonneService personneService= new PersonneService();

    @FXML
    void ajouterpersonneaction(ActionEvent event) {
        AjouterPersonne();
    }

    public void AjouterPersonne(){
        personne = new Personne(nomtextfield.getText(), prenomtextfield.getText());
        try {
            personneService.addEntity(personne);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Confirmation");
            alert.show();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/DetailsPersonne.fxml"));

            try {
                Parent root=fxmlLoader.load();
                prenomtextfield.getScene().setRoot(root);
                DetailPersonne detailPersonneController = fxmlLoader.getController();
                detailPersonneController.setNom(nomtextfield.getText());
                detailPersonneController.setPrenom(prenomtextfield.getText());
            } catch (IOException e) {
                System.out.println("...."+e.getMessage());
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR" +e.getMessage());
            alert.show();
        }
    }

}
