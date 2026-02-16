package edu.connection3a7.controller;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.Demandeservice;
import edu.connection3a7.service.Technicienserv;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class DetailDemandeController implements Initializable {

    // ========== LABELS ==========
    @FXML private Label lblId;
    @FXML private Label lblUtilisateur;
    @FXML private Label lblTechnicien;
    @FXML private Label lblTechSpecialite;
    @FXML private Label lblTechTel;
    @FXML private Label lblTechEmail;
    @FXML private Label lblType;
    @FXML private TextArea lblDescription;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;

    // ========== BOUTONS ==========
    @FXML private Button btnAccepter;
    @FXML private Button btnRefuser;
    @FXML private Button btnTerminer;
    @FXML private Button btnRetour;
    @FXML private Button btnFermer;

    // ========== SERVICES ==========
    private Demandeservice demandeService;
    private Technicienserv technicienService;

    // ========== DONNÉES ==========
    private Demande demande;
    private boolean isTechnicien;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeService = new Demandeservice();
        technicienService = new Technicienserv();
    }

    /**
     * Charger une demande par son ID
     */
    public void loadDemandeById(int idDemande, boolean technicien) {
        this.isTechnicien = technicien;
        try {
            // Récupérer la demande depuis la base
            for (Demande d : demandeService.getdata()) {
                if (d.getIdDemande() == idDemande) {
                    this.demande = d;
                    break;
                }
            }

            if (demande != null) {
                afficherDetails();
                configurerBoutons();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Demande non trouvée");
                fermer();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la demande");
            fermer();
        }
    }

    /**
     * Charger directement une demande (pour compatibilité)
     */
    public void initData(Demande demande, boolean technicien) {
        this.demande = demande;
        this.isTechnicien = technicien;
        afficherDetails();
        configurerBoutons();
    }

    /**
     * Afficher les détails de la demande
     */
    private void afficherDetails() {
        if (demande == null) return;

        lblId.setText(String.valueOf(demande.getIdDemande()));
        lblUtilisateur.setText("Utilisateur #" + demande.getIdUtilisateur());
        lblTechnicien.setText("Technicien #" + demande.getIdTech());
        lblType.setText(demande.getTypeProbleme());
        lblDescription.setText(demande.getDescription());
        lblDate.setText(demande.getDateDemande().toString());
        lblStatut.setText(demande.getStatut());

        // Récupérer les infos du technicien
        try {
            for (Technicien t : technicienService.getdata()) {
                if (t.getId_tech() == demande.getIdTech()) {
                    lblTechSpecialite.setText(t.getSpecialite());
                    lblTechTel.setText(t.getTelephone());
                    lblTechEmail.setText(t.getEmail());
                    break;
                }
            }
        } catch (SQLException e) {
            lblTechSpecialite.setText("Non disponible");
            lblTechTel.setText("Non disponible");
            lblTechEmail.setText("Non disponible");
            e.printStackTrace();
        }

        // Colorer le statut
        switch (demande.getStatut()) {
            case "En attente":
                lblStatut.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 14;");
                break;
            case "Acceptée":
                lblStatut.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14;");
                break;
            case "Refusée":
                lblStatut.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14;");
                break;
            case "Terminée":
                lblStatut.setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 14;");
                break;
        }
    }

    /**
     * Configurer les boutons selon le rôle et le statut
     */
    private void configurerBoutons() {
        if (!isTechnicien) {
            // Mode utilisateur : pas de boutons d'action
            btnAccepter.setVisible(false);
            btnRefuser.setVisible(false);
            btnTerminer.setVisible(false);
            btnRetour.setVisible(false);
            btnFermer.setVisible(true);
            return;
        }

        // Mode technicien : afficher les boutons selon le statut
        btnFermer.setVisible(false);
        btnRetour.setVisible(true);

        switch (demande.getStatut()) {
            case "En attente":
                btnAccepter.setVisible(true);
                btnRefuser.setVisible(true);
                btnTerminer.setVisible(false);
                break;
            case "Acceptée":
                btnAccepter.setVisible(false);
                btnRefuser.setVisible(false);
                btnTerminer.setVisible(true);
                break;
            default:
                btnAccepter.setVisible(false);
                btnRefuser.setVisible(false);
                btnTerminer.setVisible(false);
                break;
        }
    }

    // ========== ACTIONS ==========

    @FXML
    private void accepterDemande() {
        if (demande == null) return;

        try {
            demandeService.changerStatut(demande.getIdDemande(), "Acceptée");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande acceptée");
            fermer();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'acceptation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refuserDemande() {
        if (demande == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Refuser la demande");
        confirm.setContentText("Êtes-vous sûr de vouloir refuser cette demande ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                demandeService.changerStatut(demande.getIdDemande(), "Refusée");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande refusée");
                fermer();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du refus: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void terminerDemande() {
        if (demande == null) return;

        try {
            demandeService.changerStatut(demande.getIdDemande(), "Terminée");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande terminée");
            fermer();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la terminaison: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void retour() {
        fermer();
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) (btnFermer != null ? btnFermer.getScene().getWindow() :
                (btnRetour != null ? btnRetour.getScene().getWindow() : null));
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}