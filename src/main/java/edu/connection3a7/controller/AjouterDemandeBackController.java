package edu.connection3a7.controller;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.Demandeservice;
import edu.connection3a7.service.EmailService;
import edu.connection3a7.service.Technicienserv;
import edu.connection3a7.tools.MyConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class AjouterDemandeBackController implements Initializable {

    // ========== TABLEAU DES DEMANDES ==========
    @FXML private TableView<Demande> tableDemandesRecues;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, Integer> colUtilisateur;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, String> colDescription;
    @FXML private TableColumn<Demande, Date> colDate;
    @FXML private TableColumn<Demande, String> colStatut;

    // ========== FILTRES ET STATS ==========
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private Label lblStats;
    @FXML private Label lblInfoTechnicien;
    @FXML private Label lblNomTechnicien;
    @FXML private Label lblSpecialiteTechnicien;

    // ========== PHOTO DU TECHNICIEN CONNECTÉ ==========
    @FXML private ImageView imageTechnicien;
    @FXML private Label nomTechnicien;
    @FXML private Label specialiteTechnicien;

    // ========== BOUTONS ==========
    @FXML private Button btnActualiser;
    @FXML private Button btnVoirDetails;
    @FXML private Button btnAccepter;
    @FXML private Button btnRefuser;
    @FXML private Button btnTerminer;
    @FXML private Button btnRetour;

    // ========== SERVICES ==========
    private Demandeservice demandeService;
    private Technicienserv technicienService;
    private EmailService emailService;

    // ========== CONNEXION DB ==========
    private Connection cnx;

    // ========== VARIABLES SESSION ==========
    private int idTechnicienConnecte = 1; // À remplacer par l'ID du technicien connecté
    private Technicien technicienConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        demandeService = new Demandeservice();
        technicienService = new Technicienserv();
        emailService = new EmailService();
        this.cnx = MyConnection.getInstance().getCnx();

        chargerTechnicienConnecte();
        initTable();
        initFiltres();
        chargerDemandesRecues();

        // Désactiver les boutons par défaut
        btnVoirDetails.setDisable(true);
        btnAccepter.setDisable(true);
        btnRefuser.setDisable(true);
        btnTerminer.setDisable(true);
    }

    /**
     * Charger les informations du technicien connecté
     */
    private void chargerTechnicienConnecte() {
        try {
            // Récupérer le technicien connecté (exemple avec ID=1)
            int idTechnicienConnecte = 1; // À remplacer par l'ID de session

            for (Technicien t : technicienService.getdata()) {
                if (t.getId_tech() == idTechnicienConnecte) {
                    technicienConnecte = t;
                    break;
                }
            }

            if (technicienConnecte != null) {
                // Afficher les infos
                nomTechnicien.setText(technicienConnecte.getPrenom() + " " + technicienConnecte.getNom());
                specialiteTechnicien.setText(technicienConnecte.getSpecialite());

                // Charger la photo
                String imagePath = technicienConnecte.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imgFile = new File("src/main/resources/images/" + imagePath);
                    if (imgFile.exists()) {
                        Image image = new Image(imgFile.toURI().toString());
                        imageTechnicien.setImage(image);
                    } else {
                        // Image par défaut
                        File defaultImg = new File("src/main/resources/images/avatar.png");
                        if (defaultImg.exists()) {
                            imageTechnicien.setImage(new Image(defaultImg.toURI().toString()));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialiser la table des demandes
     */
    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDemande"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeProbleme"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage des couleurs selon le statut
        colStatut.setCellFactory(column -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "En attente":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "Acceptée":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "Refusée":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "Terminée":
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Listener pour activer/désactiver les boutons selon la sélection
        tableDemandesRecues.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        boolean isEnAttente = "En attente".equals(newVal.getStatut());
                        boolean isAcceptee = "Acceptée".equals(newVal.getStatut());

                        btnAccepter.setDisable(!isEnAttente);
                        btnRefuser.setDisable(!isEnAttente);
                        btnTerminer.setDisable(!isAcceptee);
                        btnVoirDetails.setDisable(false);
                    } else {
                        btnAccepter.setDisable(true);
                        btnRefuser.setDisable(true);
                        btnTerminer.setDisable(true);
                        btnVoirDetails.setDisable(true);
                    }
                }
        );

        // Double-clic pour voir les détails
        tableDemandesRecues.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                voirDetailDemande();
            }
        });
    }

    /**
     * Initialiser les filtres
     */
    private void initFiltres() {
        comboFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Acceptée", "Refusée", "Terminée"
        ));
        comboFiltreStatut.setValue("Tous");

        comboFiltreStatut.valueProperty().addListener(
                (obs, oldVal, newVal) -> filtrerDemandes(newVal)
        );
    }

    /**
     * Charger toutes les demandes reçues
     */
    private void chargerDemandesRecues() {
        try {
            ObservableList<Demande> demandes = FXCollections.observableArrayList(
                    demandeService.getDemandesByTechnicien(idTechnicienConnecte)
            );
            tableDemandesRecues.setItems(demandes);
            mettreAJourStats(demandes);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les demandes: " + e.getMessage());
            e.printStackTrace();

            // Données de test avec adresse
            ObservableList<Demande> testData = FXCollections.observableArrayList();
            Demande d1 = new Demande(1, 101, "Réseau", "Connexion internet très lente",
                    Date.valueOf("2026-02-13"), "En attente", 1, "Tunis");
            Demande d2 = new Demande(2, 102, "Logiciel", "Problème d'installation",
                    Date.valueOf("2026-02-12"), "Acceptée", 1, "Ariana");
            Demande d3 = new Demande(3, 103, "Matériel", "Écran qui ne s'allume pas",
                    Date.valueOf("2026-02-11"), "Terminée", 1, "La Marsa");
            testData.addAll(d1, d2, d3);
            tableDemandesRecues.setItems(testData);
            mettreAJourStats(testData);
        }
    }

    /**
     * Filtrer les demandes par statut
     */
    private void filtrerDemandes(String statut) {
        try {
            ObservableList<Demande> toutes = FXCollections.observableArrayList(
                    demandeService.getDemandesByTechnicien(idTechnicienConnecte)
            );

            if ("Tous".equals(statut)) {
                tableDemandesRecues.setItems(toutes);
            } else {
                ObservableList<Demande> filtrees = FXCollections.observableArrayList();
                for (Demande d : toutes) {
                    if (statut.equals(d.getStatut())) {
                        filtrees.add(d);
                    }
                }
                tableDemandesRecues.setItems(filtrees);
            }
            mettreAJourStats(tableDemandesRecues.getItems());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mettre à jour les statistiques
     */
    private void mettreAJourStats(ObservableList<Demande> demandes) {
        long enAttente = demandes.stream().filter(d -> "En attente".equals(d.getStatut())).count();
        long acceptees = demandes.stream().filter(d -> "Acceptée".equals(d.getStatut())).count();
        long refusees = demandes.stream().filter(d -> "Refusée".equals(d.getStatut())).count();
        long terminees = demandes.stream().filter(d -> "Terminée".equals(d.getStatut())).count();

        lblStats.setText(String.format("En attente: %d | Acceptées: %d | Refusées: %d | Terminées: %d | Total: %d",
                enAttente, acceptees, refusees, terminees, demandes.size()));
    }

    // ========== RÉCUPÉRATION EMAIL CLIENT ==========

    /**
     * Récupère l'email d'un client par son ID
     */
    // ========== RÉCUPÉRATION EMAIL CLIENT ==========

    /**
     * Récupère l'email d'un client par son ID depuis la table "utilisateurs"
     */
    private String getEmailClient(int idUtilisateur) {
        String sql = "SELECT email FROM utilisateurs WHERE id_utilisateur = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                System.out.println("📧 Email trouvé pour utilisateur " + idUtilisateur + ": " + email);
                return email;
            } else {
                System.out.println("⚠️ Aucun email trouvé pour l'utilisateur " + idUtilisateur);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ========== ENVOI EMAIL AU CLIENT ==========

    /**
     * Envoie un email au client pour l'informer du changement de statut
     */
    private void envoyerEmailClient(Demande demande, String nouveauStatut, String action) {
        String emailClient = getEmailClient(demande.getIdUtilisateur());

        if (emailClient == null || emailClient.isEmpty()) {
            System.out.println("⚠️ Email client non trouvé pour l'utilisateur " + demande.getIdUtilisateur());
            return;
        }

        String sujet = "";
        String contenu = "";

        switch (nouveauStatut) {
            case "Acceptée":
                sujet = "✅ Demande acceptée - FIRMA";
                contenu = String.format(
                        "Bonjour,\n\n" +
                                "Votre demande d'intervention #%d a été **acceptée** par le technicien.\n\n" +
                                "Détails de la demande :\n" +
                                "➤ Type : %s\n" +
                                "➤ Description : %s\n" +
                                "➤ Date : %s\n\n" +
                                "Le technicien vous contactera très prochainement pour convenir d'un rendez-vous.\n\n" +
                                "Merci de votre confiance,\n" +
                                "L'équipe FIRMA",
                        demande.getIdDemande(),
                        demande.getTypeProbleme(),
                        demande.getDescription(),
                        demande.getDateDemande()
                );
                break;

            case "Refusée":
                sujet = "❌ Demande refusée - FIRMA";
                contenu = String.format(
                        "Bonjour,\n\n" +
                                "Votre demande d'intervention #%d a été **refusée**.\n\n" +
                                "Détails de la demande :\n" +
                                "➤ Type : %s\n" +
                                "➤ Description : %s\n" +
                                "➤ Date : %s\n\n" +
                                "Vous pouvez créer une nouvelle demande ou contacter notre support pour plus d'informations.\n\n" +
                                "Cordialement,\n" +
                                "L'équipe FIRMA",
                        demande.getIdDemande(),
                        demande.getTypeProbleme(),
                        demande.getDescription(),
                        demande.getDateDemande()
                );
                break;

            case "Terminée":
                sujet = "✓ Demande terminée - FIRMA";
                contenu = String.format(
                        "Bonjour,\n\n" +
                                "Votre demande d'intervention #%d a été marquée comme **terminée**.\n\n" +
                                "Détails de la demande :\n" +
                                "➤ Type : %s\n" +
                                "➤ Description : %s\n" +
                                "➤ Date : %s\n\n" +
                                "Nous espérons que votre expérience a été satisfaisante.\n" +
                                "N'hésitez pas à laisser un avis sur le technicien qui est intervenu.\n\n" +
                                "À bientôt sur FIRMA,\n" +
                                "L'équipe FIRMA",
                        demande.getIdDemande(),
                        demande.getTypeProbleme(),
                        demande.getDescription(),
                        demande.getDateDemande()
                );
                break;
        }

        boolean envoye = emailService.envoyerEmail(emailClient, sujet, contenu);

        if (envoye) {
            System.out.println("✅ Email envoyé au client " + emailClient + " pour demande " + nouveauStatut);
        } else {
            System.out.println("❌ Échec envoi email à " + emailClient);
        }
    }

    // ========== ACTIONS ==========

    @FXML
    private void actualiser() {
        chargerDemandesRecues();
        comboFiltreStatut.setValue("Tous");
    }

    @FXML
    private void voirDetailDemande() {
        Demande selected = tableDemandesRecues.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande");
            return;
        }

        // Navigation vers les détails de la demande
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/uploads/DetailDemande.fxml"));
            Parent root = loader.load();

            DetailDemandeController controller = loader.getController();
            controller.initData(selected, true);

            Stage stage = new Stage();
            stage.setTitle("Détail demande #" + selected.getIdDemande());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails");
            e.printStackTrace();
        }
    }

    @FXML
    private void accepterDemande() {
        Demande selected = tableDemandesRecues.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Accepter la demande");
        confirm.setContentText("Voulez-vous vraiment accepter cette demande ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                demandeService.changerStatut(selected.getIdDemande(), "Acceptée");

                // 🔥 ENVOYER EMAIL AU CLIENT
                envoyerEmailClient(selected, "Acceptée", "acceptation");

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande acceptée avec succès");
                actualiser();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'acceptation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void refuserDemande() {
        Demande selected = tableDemandesRecues.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Refuser la demande");
        confirm.setContentText("Voulez-vous vraiment refuser cette demande ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                demandeService.changerStatut(selected.getIdDemande(), "Refusée");

                // 🔥 ENVOYER EMAIL AU CLIENT
                envoyerEmailClient(selected, "Refusée", "refus");

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande refusée");
                actualiser();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du refus: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void terminerDemande() {
        Demande selected = tableDemandesRecues.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Terminer la demande");
        confirm.setContentText("Voulez-vous vraiment marquer cette demande comme terminée ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                demandeService.changerStatut(selected.getIdDemande(), "Terminée");

                // 🔥 ENVOYER EMAIL AU CLIENT
                envoyerEmailClient(selected, "Terminée", "terminaison");

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande terminée");
                actualiser();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la terminaison: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Retourne vers la liste des techniciens BACK
     */
    @FXML
    private void retournerListeTechnicien() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/uploads/ListeDesTechniciens.fxml"));
            Stage stage = (Stage) tableDemandesRecues.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner");
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/uploads/DashboardTech.fxml"));
            Stage stage = (Stage) tableDemandesRecues.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard Technicien");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deconnecter() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/uploads/Login.fxml"));
                Stage stage = (Stage) tableDemandesRecues.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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