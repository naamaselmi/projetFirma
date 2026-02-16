package edu.connection3a7.controller;

import edu.connection3a7.entities.Avis;
import edu.connection3a7.service.Avisservice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterAvisSimpleController implements Initializable {

    @FXML private Label lblTechnicien;
    @FXML private ComboBox<Integer> comboNote;
    @FXML private TextArea txtCommentaire;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    private Avisservice avisService = new Avisservice();
    private Integer idUtilisateurConnecte;
    private Integer idTechnicien;
    private String nomTechnicien;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AjouterAvisSimpleController initialisé");

        // Remplir la ComboBox avec les notes de 1 à 10
        for (int i = 1; i <= 10; i++) {
            comboNote.getItems().add(i);
        }
        comboNote.setValue(5); // Note par défaut

        btnValider.setOnAction(e -> validerAvis());
        btnAnnuler.setOnAction(e -> fermer());
    }

    /**
     * Initialiser avec les données du technicien
     */
    public void initData(Integer idUser, Integer idTech, String nomTech) {
        this.idUtilisateurConnecte = idUser;
        this.idTechnicien = idTech;
        this.nomTechnicien = nomTech;

        lblTechnicien.setText("Donner votre avis sur " + nomTech);
    }

    @FXML
    private void validerAvis() {
        Integer note = comboNote.getValue();

        if (note == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une note");
            return;
        }

        try {
            Avis avis = new Avis();
            avis.setIdUtilisateur(idUtilisateurConnecte);
            avis.setNote(note);

            // Commentaire peut être vide
            String commentaire = txtCommentaire.getText();
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                avis.setCommentaire(commentaire.trim());
            }

            avis.setDateAvis(Date.valueOf(LocalDate.now()));
            avis.setIdTech(idTechnicien);
            // Pas besoin de id_demande

            avisService.addentitiy(avis);

            showAlert(Alert.AlertType.INFORMATION, "Merci !",
                    "Votre avis a été enregistré.\nNote: " + note + "/10");

            fermer();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'enregistrer l'avis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}