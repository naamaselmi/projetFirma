package edu.connection3a7.controller;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.Demandeservice;
import edu.connection3a7.service.Technicienserv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardTechController implements Initializable {

    @FXML private Label lblTotalDemandes;
    @FXML private Label lblEnAttente;
    @FXML private Label lblAcceptees;
    @FXML private Label lblRefusees;

    @FXML private TableView<Demande> tableDernieresDemandes;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, Integer> colClient;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, String> colDate;
    @FXML private TableColumn<Demande, String> colStatut;

    @FXML private Button btnDashboard;
    @FXML private Button btnDemandes;
    @FXML private Button btnInterventions;
    @FXML private Button btnProfil;
    @FXML private Button btnDeconnexion;

    private Demandeservice demandeService;
    private Technicienserv technicienService;
    private int idTechnicienConnecte = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeService = new Demandeservice();
        technicienService = new Technicienserv();

        initTable();
        chargerStatistiques();
        chargerDernieresDemandes();

        // Style du bouton actif
        btnDashboard.setStyle("-fx-background-color: #4F772D; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        System.out.println("✅ DashboardTechController initialisé");
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDemande"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeProbleme"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Double-clic pour voir les détails
        tableDernieresDemandes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                voirDetailDemande();
            }
        });
    }

    private void chargerStatistiques() {
        try {
            ObservableList<Demande> demandes = FXCollections.observableArrayList(
                    demandeService.getDemandesByTechnicien(idTechnicienConnecte)
            );

            long total = demandes.size();
            long enAttente = demandes.stream().filter(d -> "En attente".equals(d.getStatut())).count();
            long acceptees = demandes.stream().filter(d -> "Acceptée".equals(d.getStatut())).count();
            long refusees = demandes.stream().filter(d -> "Refusée".equals(d.getStatut())).count();

            lblTotalDemandes.setText(String.valueOf(total));
            lblEnAttente.setText(String.valueOf(enAttente));
            lblAcceptees.setText(String.valueOf(acceptees));
            lblRefusees.setText(String.valueOf(refusees));

        } catch (SQLException e) {
            e.printStackTrace();
            // Données de test
            lblTotalDemandes.setText("5");
            lblEnAttente.setText("2");
            lblAcceptees.setText("2");
            lblRefusees.setText("1");
        }
    }

    private void chargerDernieresDemandes() {
        try {
            ObservableList<Demande> toutes = FXCollections.observableArrayList(
                    demandeService.getDemandesByTechnicien(idTechnicienConnecte)
            );

            ObservableList<Demande> dernieres = FXCollections.observableArrayList();
            for (int i = 0; i < Math.min(5, toutes.size()); i++) {
                dernieres.add(toutes.get(i));
            }

            tableDernieresDemandes.setItems(dernieres);

        } catch (SQLException e) {
            e.printStackTrace();
            // Données de test
            ObservableList<Demande> testData = FXCollections.observableArrayList();
            Demande d1 = new Demande();
            d1.setIdDemande(1);
            d1.setIdUtilisateur(101);
            d1.setTypeProbleme("Réseau");
            d1.setDescription("Connexion lente");
            d1.setDateDemande(java.sql.Date.valueOf("2026-02-13"));
            d1.setStatut("En attente");
            d1.setIdTech(1);

            Demande d2 = new Demande();
            d2.setIdDemande(2);
            d2.setIdUtilisateur(102);
            d2.setTypeProbleme("Logiciel");
            d2.setDescription("Problème installation");
            d2.setDateDemande(java.sql.Date.valueOf("2026-02-12"));
            d2.setStatut("Acceptée");
            d2.setIdTech(1);

            testData.addAll(d1, d2);
            tableDernieresDemandes.setItems(testData);
        }
    }

    @FXML
    private void voirDetailDemande() {
        Demande selected = tableDernieresDemandes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationBack.openDetailDemande(selected.getIdDemande(), true);
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande");
        }
    }

    @FXML
    private void goToListeDemandes() {
        NavigationBack.goToListeDemandesBack();
    }

    @FXML
    private void goToInterventions() {
        NavigationBack.goToMesInterventions();
    }

    @FXML
    private void goToProfil() {
        NavigationBack.goToProfilTechnicien();
    }

    @FXML
    private void retourAccueil() {
        NavigationBack.retourAccueil();
    }

    @FXML
    private void deconnecter() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            NavigationBack.logout();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}