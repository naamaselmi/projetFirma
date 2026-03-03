package Firma.controllers.GestionMarketplace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import Firma.entities.GestionMarketplace.Commande;
import Firma.entities.GestionMarketplace.DeliveryStatus;
import Firma.entities.GestionMarketplace.PaymentStatus;
import Firma.services.GestionMarketplace.CommandeService;
import Firma.services.GestionMarketplace.UtilisateurService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

public class CommandeAdminController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> paymentFilterCombo;
    @FXML private ComboBox<String> deliveryFilterCombo;
    @FXML private Label totalCountLabel;
    @FXML private Label paidCountLabel;
    @FXML private Label pendingCountLabel;
    
    @FXML private TableView<Commande> commandeTable;
    @FXML private TableColumn<Commande, String> colId;
    @FXML private TableColumn<Commande, String> colNumero;
    @FXML private TableColumn<Commande, String> colClient;
    @FXML private TableColumn<Commande, String> colMontant;
    @FXML private TableColumn<Commande, String> colPaiement;
    @FXML private TableColumn<Commande, String> colLivraison;
    @FXML private TableColumn<Commande, String> colAdresse;
    @FXML private TableColumn<Commande, String> colVille;
    @FXML private TableColumn<Commande, String> colDate;
    @FXML private TableColumn<Commande, Void> colActions;

    private CommandeService commandeService;
    private UtilisateurService utilisateurService;
    private ObservableList<Commande> commandeList;
    private FilteredList<Commande> filteredList;
    private Map<Integer, String> userNameCache;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commandeService = new CommandeService();
        utilisateurService = new UtilisateurService();
        commandeList = FXCollections.observableArrayList();
        userNameCache = new HashMap<>();
        
        setupFilters();
        setupTableColumns();
        loadCommandes();
        setupSearch();
    }

    private void setupFilters() {
        // Payment status filter
        paymentFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "En attente", "Payé", "Échoué"
        ));
        paymentFilterCombo.setValue("Tous");
        paymentFilterCombo.setOnAction(e -> applyFilters());
        
        // Delivery status filter
        deliveryFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "En attente", "En préparation", "Expédié", "Livré", "Annulé"
        ));
        deliveryFilterCombo.setValue("Tous");
        deliveryFilterCombo.setOnAction(e -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        
        colNumero.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNumeroCommande()));
        
        colClient.setCellValueFactory(data -> {
            int userId = data.getValue().getUtilisateurId();
            String userName = getUserName(userId);
            return new SimpleStringProperty(userName);
        });
        
        colMontant.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getMontantTotal() + " DT"));
        
        colPaiement.setCellValueFactory(data -> 
            new SimpleStringProperty(getPaymentStatusDisplay(data.getValue().getStatutPaiement())));
        colPaiement.setCellFactory(col -> createStatusCell(true));
        
        colLivraison.setCellValueFactory(data -> 
            new SimpleStringProperty(getDeliveryStatusDisplay(data.getValue().getStatutLivraison())));
        colLivraison.setCellFactory(col -> createStatusCell(false));
        
        colAdresse.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAdresseLivraison()));
        
        colVille.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getVilleLivraison()));
        
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateCommande() != null) {
                return new SimpleStringProperty(data.getValue().getDateCommande().format(DATE_FORMAT));
            }
            return new SimpleStringProperty("");
        });
        
        setupActionsColumn();
    }

    private TableCell<Commande, String> createStatusCell(boolean isPayment) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-padding: 5px; -fx-background-radius: 3;";
                    if (isPayment) {
                        switch (item) {
                            case "Payé" -> setStyle(style + "-fx-text-fill: #27ae60;");
                            case "En attente" -> setStyle(style + "-fx-text-fill: #f39c12;");
                            case "Échoué" -> setStyle(style + "-fx-text-fill: #e74c3c;");
                            default -> setStyle(style + "-fx-text-fill: #666;");
                        }
                    } else {
                        switch (item) {
                            case "Livré" -> setStyle(style + "-fx-text-fill: #27ae60;");
                            case "Expédié" -> setStyle(style + "-fx-text-fill: #3498db;");
                            case "En préparation" -> setStyle(style + "-fx-text-fill: #9b59b6;");
                            case "En attente" -> setStyle(style + "-fx-text-fill: #f39c12;");
                            case "Annulé" -> setStyle(style + "-fx-text-fill: #e74c3c;");
                            default -> setStyle(style + "-fx-text-fill: #666;");
                        }
                    }
                }
            }
        };
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnPaiement = new Button("💳");
            private final Button btnLivraison = new Button("🚚");
            private final Button btnDetails = new Button("📋");
            
            {
                btnPaiement.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                btnLivraison.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                btnDetails.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                
                btnPaiement.setTooltip(new Tooltip("Modifier statut paiement"));
                btnLivraison.setTooltip(new Tooltip("Modifier statut livraison"));
                btnDetails.setTooltip(new Tooltip("Voir détails"));
                
                btnPaiement.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleUpdatePayment(commande);
                });
                
                btnLivraison.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleUpdateDelivery(commande);
                });
                
                btnDetails.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleShowDetails(commande);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, btnPaiement, btnLivraison, btnDetails);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void handleUpdatePayment(Commande commande) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            getPaymentStatusDisplay(commande.getStatutPaiement()),
            "En attente", "Payé", "Échoué"
        );
        dialog.setTitle("Modifier Paiement");
        dialog.setHeaderText("Commande: " + commande.getNumeroCommande());
        dialog.setContentText("Nouveau statut de paiement:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            try {
                PaymentStatus newStatus = switch (status) {
                    case "Payé" -> PaymentStatus.PAYE;
                    case "Échoué" -> PaymentStatus.ECHOUE;
                    default -> PaymentStatus.EN_ATTENTE;
                };
                commandeService.updatePaymentStatus(commande.getId(), newStatus);
                loadCommandes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Statut de paiement mis à jour avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la mise à jour: " + e.getMessage());
            }
        });
    }

    private void handleUpdateDelivery(Commande commande) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            getDeliveryStatusDisplay(commande.getStatutLivraison()),
            "En attente", "En préparation", "Expédié", "Livré", "Annulé"
        );
        dialog.setTitle("Modifier Livraison");
        dialog.setHeaderText("Commande: " + commande.getNumeroCommande());
        dialog.setContentText("Nouveau statut de livraison:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            try {
                DeliveryStatus newStatus = switch (status) {
                    case "En préparation" -> DeliveryStatus.EN_PREPARATION;
                    case "Expédié" -> DeliveryStatus.EXPEDIE;
                    case "Livré" -> DeliveryStatus.LIVRE;
                    case "Annulé" -> DeliveryStatus.ANNULE;
                    default -> DeliveryStatus.EN_ATTENTE;
                };
                commandeService.updateDeliveryStatus(commande.getId(), newStatus);
                loadCommandes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Statut de livraison mis à jour avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la mise à jour: " + e.getMessage());
            }
        });
    }

    private void handleShowDetails(Commande commande) {
        StringBuilder details = new StringBuilder();
        details.append("Numéro: ").append(commande.getNumeroCommande()).append("\n");
        details.append("Client: ").append(getUserName(commande.getUtilisateurId())).append("\n");
        details.append("Montant: ").append(commande.getMontantTotal()).append(" DT\n");
        details.append("Paiement: ").append(getPaymentStatusDisplay(commande.getStatutPaiement())).append("\n");
        details.append("Livraison: ").append(getDeliveryStatusDisplay(commande.getStatutLivraison())).append("\n");
        details.append("Adresse: ").append(commande.getAdresseLivraison()).append("\n");
        details.append("Ville: ").append(commande.getVilleLivraison()).append("\n");
        if (commande.getDateCommande() != null) {
            details.append("Date: ").append(commande.getDateCommande().format(DATE_FORMAT)).append("\n");
        }
        if (commande.getNotes() != null && !commande.getNotes().isEmpty()) {
            details.append("Notes: ").append(commande.getNotes()).append("\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Commande");
        alert.setHeaderText("Commande " + commande.getNumeroCommande());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void loadCommandes() {
        try {
            commandeList.clear();
            commandeList.addAll(commandeService.getEntities());
            
            filteredList = new FilteredList<>(commandeList, p -> true);
            commandeTable.setItems(filteredList);
            
            updateStats();
            applyFilters();
            commandeTable.refresh();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredList == null) return;
        
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String paymentFilter = paymentFilterCombo.getValue();
        String deliveryFilter = deliveryFilterCombo.getValue();
        
        filteredList.setPredicate(commande -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                commande.getNumeroCommande().toLowerCase().contains(searchText) ||
                getUserName(commande.getUtilisateurId()).toLowerCase().contains(searchText);
            
            // Payment filter
            boolean matchesPayment = paymentFilter == null || paymentFilter.equals("Tous") ||
                getPaymentStatusDisplay(commande.getStatutPaiement()).equals(paymentFilter);
            
            // Delivery filter
            boolean matchesDelivery = deliveryFilter == null || deliveryFilter.equals("Tous") ||
                getDeliveryStatusDisplay(commande.getStatutLivraison()).equals(deliveryFilter);
            
            return matchesSearch && matchesPayment && matchesDelivery;
        });
        
        updateStats();
    }

    private void updateStats() {
        int total = commandeList.size();
        long paid = commandeList.stream()
            .filter(c -> c.getStatutPaiement() == PaymentStatus.PAYE)
            .count();
        long pending = commandeList.stream()
            .filter(c -> c.getStatutPaiement() == PaymentStatus.EN_ATTENTE)
            .count();
        
        totalCountLabel.setText("Total: " + total);
        paidCountLabel.setText("Payées: " + paid);
        pendingCountLabel.setText("En attente: " + pending);
    }

    private String getUserName(int userId) {
        if (userNameCache.containsKey(userId)) {
            return userNameCache.get(userId);
        }
        try {
            Firma.entities.GestionMarketplace.Utilisateur user = utilisateurService.getById(userId);
            if (user != null) {
                String name = user.getPrenom() + " " + user.getNom();
                userNameCache.put(userId, name);
                return name;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return "Utilisateur #" + userId;
    }

    private String getPaymentStatusDisplay(PaymentStatus status) {
        if (status == null) return "Inconnu";
        return switch (status) {
            case PAYE -> "Payé";
            case EN_ATTENTE -> "En attente";
            case ECHOUE -> "Échoué";
            case PARTIEL -> "Partiel";
        };
    }

    private String getDeliveryStatusDisplay(DeliveryStatus status) {
        if (status == null) return "Inconnu";
        return switch (status) {
            case EN_ATTENTE -> "En attente";
            case EN_PREPARATION -> "En préparation";
            case EXPEDIE -> "Expédié";
            case LIVRE -> "Livré";
            case ANNULE -> "Annulé";
        };
    }

    @FXML
    void handleRefresh() {
        loadCommandes();
    }

    @FXML
    void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les commandes en CSV");
        fileChooser.setInitialFileName("commandes_export_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(commandeTable.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("ID;Numéro;Client;Montant;Paiement;Livraison;Adresse;Ville;Date\n");
                
                for (Commande c : filteredList) {
                    writer.write(String.format("%d;%s;%s;%s;%s;%s;%s;%s;%s\n",
                        c.getId(),
                        c.getNumeroCommande(),
                        getUserName(c.getUtilisateurId()),
                        c.getMontantTotal(),
                        getPaymentStatusDisplay(c.getStatutPaiement()),
                        getDeliveryStatusDisplay(c.getStatutLivraison()),
                        c.getAdresseLivraison() != null ? c.getAdresseLivraison() : "",
                        c.getVilleLivraison() != null ? c.getVilleLivraison() : "",
                        c.getDateCommande() != null ? c.getDateCommande().format(DATE_FORMAT) : ""
                    ));
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Export réussi", 
                    "Fichier exporté: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de l'export: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
