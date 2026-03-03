package Firma.controllers.GestionTechnicien;

import Firma.entities.GestionTechnicien.Demande;
import Firma.entities.GestionTechnicien.Technicien;
import Firma.services.GestionTechnicien.*;
import Firma.tools.GestionEvenement.MyConnection;
import Firma.tools.GestionEvenement.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class AjouterDemandeFrontController implements Initializable {

    // ========== FORMULAIRE ==========
    @FXML private ComboBox<Technicien> comboTechniciens;
    @FXML private ComboBox<String> comboTypeProbleme;
    @FXML private TextField txtDate;
    @FXML private TextArea txtDescription;
    @FXML private Label lblInfoTech;

    // ========== TABLEAU DES DEMANDES ==========
    @FXML private TableView<Demande> tableMesDemandes;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, String> colDescription;
    @FXML private TableColumn<Demande, Date> colDate;
    @FXML private TableColumn<Demande, String> colStatut;
    @FXML private TableColumn<Demande, Integer> colTech;

    // ========== BOUTONS ==========
    @FXML private Button btnAnnuler;
    @FXML private Button btnSoumettre;
    @FXML private Button btnActualiser;
    @FXML private Button btnVoirDetails;
    @FXML private Button btnDeconnexion;
    @FXML private Button btnSoumettreManuel;
    @FXML private Button btnAutoAssigner;

    // ========== SERVICES ==========
    private final Demandeservice demandeService = new Demandeservice();
    private final Technicienserv technicienService = new Technicienserv();
    private final AutoAssignationService autoService = new AutoAssignationService();
    private final NotificationService notificationService = new NotificationService();
    private final EmailServiceTechnicien emailService = new EmailServiceTechnicien();

    // ========== CONNEXION BASE DE DONNÉES ==========
    private Connection cnx;

    // ========== VARIABLES SESSION ==========
    private int idUtilisateurConnecte;
    private Technicien technicienChoisi = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser la connexion à la base de données
        this.cnx = MyConnection.getInstance().getCnx();

        // Récupérer l'ID de l'utilisateur connecté depuis SessionManager
        this.idUtilisateurConnecte = (SessionManager.getInstance().getUtilisateur() != null ? SessionManager.getInstance().getUtilisateur().getId() : 0);

        System.out.println("✅ AjouterDemandeFrontController initialisé");
        System.out.println("   Utilisateur connecté ID: " + idUtilisateurConnecte);
        System.out.println("   📧 Service email activé");

        // Initialisation des composants
        initialiserComboBoxes();
        initialiserTable();
        chargerDonnees();
        setDateAujourdhui();

        // Si un technicien a été pré-sélectionné, on le sélectionne
        if (technicienChoisi != null) {
            comboTechniciens.getSelectionModel().select(technicienChoisi);
            afficherInfosTechnicien(technicienChoisi);
        }
    }

    /**
     * ✅ Méthode appelée depuis la liste front pour pré-sélectionner un technicien
     */
    public void setTechnicienChoisi(Technicien tech) {
        this.technicienChoisi = tech;
        if (comboTechniciens != null) {
            comboTechniciens.getSelectionModel().select(tech);
            afficherInfosTechnicien(tech);
        }
        System.out.println("✅ Technicien pré-sélectionné: " + tech.getPrenom() + " " + tech.getNom());
    }

    /**
     * Initialise les ComboBox
     */
    private void initialiserComboBoxes() {
        // Types de problèmes
        ObservableList<String> typesProblemes = FXCollections.observableArrayList(
                "Réseau", "Logiciel", "Installation", "Maintenance",
                "Matériel", "Dépannage", "Autre"
        );
        comboTypeProbleme.setItems(typesProblemes);

        // Affichage des techniciens
        comboTechniciens.setCellFactory(param -> new ListCell<Technicien>() {
            @Override
            protected void updateItem(Technicien tech, boolean empty) {
                super.updateItem(tech, empty);
                if (empty || tech == null) {
                    setText(null);
                } else {
                    String dispo = tech.isDisponibilite() ? "✅" : "❌";
                    setText(dispo + " " + tech.getPrenom() + " " + tech.getNom() + " - " + tech.getSpecialite());
                }
            }
        });

        comboTechniciens.setButtonCell(new ListCell<Technicien>() {
            @Override
            protected void updateItem(Technicien tech, boolean empty) {
                super.updateItem(tech, empty);
                if (empty || tech == null) {
                    setText("Choisissez un technicien");
                } else {
                    setText(tech.getPrenom() + " " + tech.getNom());
                }
            }
        });

        // Listener pour afficher les infos du technicien
        comboTechniciens.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        afficherInfosTechnicien(newVal);
                    } else {
                        lblInfoTech.setText("");
                    }
                }
        );
    }

    /**
     * Affiche les informations détaillées du technicien
     */
    private void afficherInfosTechnicien(Technicien tech) {
        String dispo = tech.isDisponibilite() ? "✅ Disponible" : "❌ Non disponible";
        lblInfoTech.setText(
                "🔧 Spécialité: " + tech.getSpecialite() +
                        " | 📞 Tél: " + (tech.getTelephone() != null ? tech.getTelephone() : "Non renseigné") +
                        " | 📍 " + (tech.getLocalisation() != null ? tech.getLocalisation() : "Non renseignée") +
                        " | " + dispo +
                        " | 📧 Email: " + tech.getEmail()
        );
    }

    /**
     * Initialise la table des demandes
     */
    private void initialiserTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDemande"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeProbleme"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colTech.setCellValueFactory(new PropertyValueFactory<>("idTech"));

        // Couleurs selon le statut
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

        // Double-clic pour détails
        tableMesDemandes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                voirDetailDemande();
            }
        });
    }

    /**
     * Charge toutes les données
     */
    private void chargerDonnees() {
        chargerTechniciensDisponibles();
        chargerMesDemandes();
    }

    /**
     * Date du jour
     */
    private void setDateAujourdhui() {
        txtDate.setText(LocalDate.now().toString());
        txtDate.setEditable(false);
    }

    /**
     * Charge les techniciens disponibles
     */
    private void chargerTechniciensDisponibles() {
        try {
            ObservableList<Technicien> techniciens = FXCollections.observableArrayList();
            for (Technicien t : technicienService.getdata()) {
                techniciens.add(t);
            }
            comboTechniciens.setItems(techniciens);

            if (techniciens.isEmpty()) {
                lblInfoTech.setText("Aucun technicien disponible");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les techniciens");
            e.printStackTrace();
        }
    }

    /**
     * Charge les demandes de l'utilisateur
     */
    private void chargerMesDemandes() {
        try {
            ObservableList<Demande> demandes = FXCollections.observableArrayList(
                    demandeService.getDemandesByUtilisateur(idUtilisateurConnecte)
            );
            tableMesDemandes.setItems(demandes);
            System.out.println("📊 " + demandes.size() + " demande(s) chargée(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger vos demandes");
            e.printStackTrace();
        }
    }

    // ========== RÉCUPÉRATION DE L'EMAIL DU CLIENT ==========

    /**
     * Récupère l'email du client connecté depuis la base de données
     * @return L'email du client ou un email par défaut
     */
    private String getEmailClient() {
        // Vérifier que la connexion est initialisée
        if (cnx == null) {
            cnx = MyConnection.getInstance().getCnx();
        }

        try {
            String sql = "SELECT email FROM utilisateurs WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, idUtilisateurConnecte);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                System.out.println("📧 Email client récupéré: " + email);
                rs.close();
                ps.close();
                return email;
            } else {
                System.out.println("⚠️ Aucun email trouvé pour l'utilisateur " + idUtilisateurConnecte);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'email: " + e.getMessage());
            e.printStackTrace();
        }

        // Email par défaut si la requête échoue
        System.out.println("📧 Utilisation de l'email par défaut: molkaajengui@gmail.com");
        return "molkaajengui@gmail.com";
    }

    // ========== SOUMISSION MANUELLE ==========

    @FXML
    private void soumettreDemandeManuelle() {
        // Validation
        Technicien tech = comboTechniciens.getValue();
        String type = comboTypeProbleme.getValue();
        String description = txtDescription.getText();

        if (tech == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un technicien");
            return;
        }

        if (type == null || type.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner le type de problème");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez décrire votre problème");
            return;
        }

        if (description.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 10 caractères");
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setIdUtilisateur(idUtilisateurConnecte);
            demande.setIdTech(tech.getId_tech());
            demande.setTypeProbleme(type);
            demande.setDescription(description);
            demande.setDateDemande(Date.valueOf(LocalDate.now()));
            demande.setStatut("En attente");

            demandeService.addentitiy(demande);

            // 🔥 ENVOI DES EMAILS AUTOMATIQUES
            String emailClient = getEmailClient();
            notificationService.envoyerConfirmationClient(demande, emailClient);
            notificationService.envoyerNotificationTechnicien(tech, demande);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "✅ Demande envoyée avec succès au technicien " + tech.getPrenom() + " " + tech.getNom() +
                            "\n📧 Un email de confirmation vous a été envoyé à: " + emailClient);

            // Réinitialiser
            annuler();
            chargerMesDemandes();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== SOUMISSION AVEC AUTO-ASSIGNATION ==========

    @FXML
    private void soumettreDemandeAuto() {
        String type = comboTypeProbleme.getValue();
        String description = txtDescription.getText();

        if (type == null || type.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner le type de problème");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez décrire votre problème");
            return;
        }

        if (description.length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 10 caractères");
            return;
        }

        try {
            // 1. Créer la demande
            Demande nouvelleDemande = new Demande();
            nouvelleDemande.setIdUtilisateur(idUtilisateurConnecte);
            nouvelleDemande.setTypeProbleme(type);
            nouvelleDemande.setDescription(description);
            nouvelleDemande.setDateDemande(Date.valueOf(LocalDate.now()));
            nouvelleDemande.setStatut("En attente");

            // 2. AUTO-ASSIGNATION
            System.out.println("\n🚀 AUTO-ASSIGNATION EN COURS...");
            var meilleur = autoService.trouverMeilleurTechnicien(nouvelleDemande);

            StringBuilder message = new StringBuilder();

            if (meilleur != null && meilleur.getScore() >= 50) {
                // Assigner au meilleur technicien
                Technicien techChoisi = meilleur.getTechnicien();
                nouvelleDemande.setIdTech(techChoisi.getId_tech());
                nouvelleDemande.setStatut("Acceptée");

                // Sauvegarder
                demandeService.addentitiy(nouvelleDemande);

                // 🔥 ENVOI DES EMAILS AUTOMATIQUES
                String emailClient = getEmailClient();
                notificationService.envoyerConfirmationClient(nouvelleDemande, emailClient);
                notificationService.envoyerNotificationTechnicien(techChoisi, nouvelleDemande);

                message.append("✅ Demande acceptée !\n\n")
                        .append("👨‍🔧 Technicien: ").append(techChoisi.getPrenom())
                        .append(" ").append(techChoisi.getNom()).append("\n")
                        .append("📊 Score: ").append(String.format("%.1f", meilleur.getScore())).append("/100\n")
                        .append("🔧 Spécialité: ").append(techChoisi.getSpecialite()).append("\n\n")
                        .append("📧 Un email de confirmation vous a été envoyé à: ").append(emailClient).append("\n")
                        .append("Le technicien a également été notifié.");

                System.out.println("✅ Assigné à " + techChoisi.getNom());
                System.out.println("📧 Emails envoyés - Client et technicien notifiés");

            } else {
                // Pas de technicien disponible
                nouvelleDemande.setStatut("En attente");
                demandeService.addentitiy(nouvelleDemande);

                // 🔥 ENVOI EMAIL D'ATTENTE AU CLIENT
                String emailClient = getEmailClient();
                emailService.envoyerEmail(
                        emailClient,
                        "⏳ Demande en attente - FIRMA",
                        "Bonjour,\n\nVotre demande a été enregistrée mais aucun technicien n'est disponible pour le moment.\n\n" +
                                "Vous recevrez une notification dès qu'un technicien sera assigné.\n\nCordialement,\nL'équipe FIRMA"
                );

                message.append("⏳ Demande enregistrée.\n\n")
                        .append("Aucun technicien n'est disponible actuellement.\n")
                        .append("Votre demande a été mise en attente. Vous recevrez un email dès qu'un technicien sera disponible.");

                System.out.println("⏳ Demande mise en attente - email envoyé au client");
            }

            // Afficher le résultat
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résultat de la demande");
            alert.setHeaderText(null);
            alert.setContentText(message.toString());

            // Ajouter une zone de texte détaillée si disponible
            if (meilleur != null) {
                TextArea textArea = new TextArea(meilleur.getDetailsString());
                textArea.setEditable(false);
                textArea.setPrefHeight(150);
                alert.getDialogPane().setExpandableContent(textArea);
            }

            alert.showAndWait();

            // Réinitialiser le formulaire
            annuler();
            chargerMesDemandes();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Annuler le formulaire
     */
    @FXML
    private void annuler() {
        comboTechniciens.getSelectionModel().clearSelection();
        comboTypeProbleme.getSelectionModel().clearSelection();
        txtDescription.clear();
        lblInfoTech.setText("");
    }

    /**
     * Voir détails d'une demande
     */
    @FXML
    private void voirDetailDemande() {
        Demande selected = tableMesDemandes.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionTechnicien/DetailDemande.fxml"));
            Parent root = loader.load();

            DetailDemandeController controller = loader.getController();
            controller.initData(selected, false);

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

    /**
     * Rafraîchir les données
     */
    @FXML
    private void rafraichir() {
        chargerDonnees();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Liste actualisée");
    }

    /**
     * Retourne vers la liste des techniciens FRONT
     */
    @FXML
    private void retournerListeFront() {
        try {
            System.out.println("🔄 Retour vers la liste FRONT");

            URL fxmlUrl = getClass().getResource("/GestionTechnicien/ListeDesTechniciensfront.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier ListeDesTechniciensfront.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) comboTechniciens.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens");
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner: " + e.getMessage());
        }
    }

    /**
     * Déconnexion
     */
    @FXML
    private void deconnecter() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirm.setContentText("Vous serez redirigé vers la page de connexion.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginApplication.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) comboTechniciens.getScene().getWindow();
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

    // ========== GETTERS/SETTERS ==========
    public void setIdUtilisateurConnecte(int id) {
        this.idUtilisateurConnecte = id;
    }

    public int getIdUtilisateurConnecte() {
        return idUtilisateurConnecte;
    }
}