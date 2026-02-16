package edu.connection3a7.controller;

import edu.connection3a7.entities.Avis;
import edu.connection3a7.service.Avisservice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierAvisController implements Initializable {

    @FXML private ComboBox<Integer> comboNote;
    @FXML private TextArea txtCommentaire;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    private Avisservice avisService = new Avisservice();
    private Avis avisAModifier;
    private Runnable onAvisModifie;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (int i = 1; i <= 10; i++) {
            comboNote.getItems().add(i);
        }

        btnValider.setOnAction(e -> valider());
        btnAnnuler.setOnAction(e -> fermer());
    }

    public void initData(Avis avis, Runnable callback) {
        this.avisAModifier = avis;
        this.onAvisModifie = callback;

        comboNote.setValue(avis.getNote());
        txtCommentaire.setText(avis.getCommentaire());
    }

    private void valider() {
        try {
            avisAModifier.setNote(comboNote.getValue());
            avisAModifier.setCommentaire(txtCommentaire.getText());

            avisService.update(avisAModifier);

            if (onAvisModifie != null) {
                onAvisModifie.run();
            }

            fermer();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fermer() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}