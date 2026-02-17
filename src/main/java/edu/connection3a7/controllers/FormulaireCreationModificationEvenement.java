package edu.connection3a7.controllers;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Statutevent;
import edu.connection3a7.entities.Type;
import edu.connection3a7.services.EvenementService;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Logique de création, modification, annulation et suppression d'événements.
 * Les méthodes @FXML restent dans EvenementController et délèguent ici.
 */
public class FormulaireCreationModificationEvenement {

    private final EvenementController controller;

    FormulaireCreationModificationEvenement(EvenementController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private EvenementService service()            { return controller.getEvenementService(); }
    private DateTimeFormatter timeFmt()            { return controller.getTimeFmt(); }

    // ============================================================
    //  CRÉER
    // ============================================================

    void ajouterImage() {
        File file = choisirImage();
        if (file != null) {
            controller.setCreateImagePath(file.getAbsolutePath());
            controller.getCreateImageLabel().setText(file.getName());
            controller.getCreateImageLabel().setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #333; -fx-background-color: #f5f5f5; "
                    + "-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #c8c8c8; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 5;");
        }
    }

    void creerEvenement() {
        TextField createTitre = controller.getCreateTitre();
        TextArea createDescription = controller.getCreateDescription();
        TextField createOrganisateur = controller.getCreateOrganisateur();
        ComboBox<Type> createType = controller.getCreateType();
        DatePicker createDateDebut = controller.getCreateDateDebut();
        DatePicker createDateFin = controller.getCreateDateFin();
        TextField createTempsDebut = controller.getCreateTempsDebut();
        TextField createTempsFin = controller.getCreateTempsFin();
        TextField createNombrePlaces = controller.getCreateNombrePlaces();
        TextField createLieu = controller.getCreateLieu();
        TextField createAdresse = controller.getCreateAdresse();

        if (createTitre.getText().isBlank() || createDateDebut.getValue() == null
                || createDateFin.getValue() == null || createNombrePlaces.getText().isBlank()) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir : Titre, Dates et Nombre de places.");
            return;
        }
        if (createTitre.getText().trim().length() < 3) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Titre trop court",
                    "Le titre doit contenir au moins 3 caracteres.");
            return;
        }
        if (createTitre.getText().trim().length() > 100) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Titre trop long",
                    "Le titre ne doit pas depasser 100 caracteres.");
            return;
        }
        if (createType.getValue() == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Type manquant",
                    "Veuillez selectionner un type d'evenement.");
            return;
        }
        if (createDateFin.getValue().isBefore(createDateDebut.getValue())) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Dates invalides",
                    "La date de fin doit etre egale ou posterieure a la date de debut.");
            return;
        }
        if (createDateDebut.getValue().isBefore(java.time.LocalDate.now())) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Date invalide",
                    "La date de debut ne peut pas etre dans le passe.");
            return;
        }
        LocalTime heureDebut = parseTime(createTempsDebut.getText());
        LocalTime heureFin   = parseTime(createTempsFin.getText());
        if (heureDebut == null || heureFin == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide",
                    "Format attendu : HH:mm (ex: 09:30)");
            return;
        }
        if (createDateDebut.getValue().equals(createDateFin.getValue()) && !heureFin.isAfter(heureDebut)) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide",
                    "L'horaire de fin doit etre apres l'horaire de debut lorsque les dates sont identiques.");
            return;
        }
        try {
            int nbPlaces = Integer.parseInt(createNombrePlaces.getText().trim());
            if (nbPlaces <= 0) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Places invalides",
                        "Le nombre de places doit etre superieur a 0.");
                return;
            }
            if (nbPlaces > 100000) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Places invalides",
                        "Le nombre de places ne peut pas depasser 100 000.");
                return;
            }
        } catch (NumberFormatException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Le nombre de places doit etre un entier valide.");
            return;
        }
        if (createOrganisateur.getText().isBlank()) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Champ manquant",
                    "Veuillez saisir le nom de l'organisateur.");
            return;
        }

        try {
            Evenement e = new Evenement();
            e.setTitre(createTitre.getText().trim());
            e.setDescription(createDescription.getText().trim());
            e.setOrganisateur(createOrganisateur.getText().trim());
            e.setTypeEvenement(createType.getValue());
            e.setDateDebut(createDateDebut.getValue());
            e.setDateFin(createDateFin.getValue());
            e.setHoraireDebut(heureDebut);
            e.setHoraireFin(heureFin);
            e.setCapaciteMax(Integer.parseInt(createNombrePlaces.getText().trim()));
            e.setPlacesDisponibles(Integer.parseInt(createNombrePlaces.getText().trim()));
            e.setLieu(createLieu.getText().trim());
            e.setAdresse(createAdresse.getText().trim());
            e.setImageUrl(controller.getCreateImagePath());
            e.setStatut(Statutevent.actif);

            service().addEntity(e);
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                    "Événement créé avec succès !");
            clearCreateForm();
            controller.chargerListe(null);
            controller.getMainTabPane().getSelectionModel().select(0);

        } catch (NumberFormatException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Le nombre de places doit être un entier.");
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur base de données",
                    ex.getMessage());
        }
    }

    // ============================================================
    //  MODIFIER
    // ============================================================

    void ouvrirModifier(Evenement e) {
        controller.setSelectedEvenementId(e.getIdEvenement());
        controller.getModifyTitre().setText(e.getTitre());
        controller.getModifyDescription().setText(OutilsInterfaceGraphique.nullSafe(e.getDescription()));
        controller.getModifyOrganisateur().setText(OutilsInterfaceGraphique.nullSafe(e.getOrganisateur()));
        controller.getModifyType().setValue(e.getTypeEvenement());
        controller.getModifyDateDebut().setValue(e.getDateDebut());
        controller.getModifyDateFin().setValue(e.getDateFin());
        controller.getModifyTempsDebut().setText(
                e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt()) : "");
        controller.getModifyTempsFin().setText(
                e.getHoraireFin() != null ? e.getHoraireFin().format(timeFmt()) : "");
        controller.getModifyNombrePlaces().setText(String.valueOf(e.getCapaciteMax()));
        controller.getModifyLieu().setText(OutilsInterfaceGraphique.nullSafe(e.getLieu()));
        controller.getModifyAdresse().setText(OutilsInterfaceGraphique.nullSafe(e.getAdresse()));
        controller.setModifyImagePath(e.getImageUrl());
        String mip = e.getImageUrl();
        if (mip != null && !mip.isBlank()) {
            controller.getModifyImageLabel().setText(new File(mip).getName());
            controller.getModifyImageLabel().setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #333; -fx-background-color: #f5f5f5; "
                    + "-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #c8c8c8; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 5;");
        } else {
            controller.getModifyImageLabel().setText("Aucune image sélectionnée");
            controller.getModifyImageLabel().setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #888; -fx-background-color: #f5f5f5; "
                    + "-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #c8c8c8; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 5;");
        }
        controller.getMainTabPane().getSelectionModel().select(2);
    }

    void rechercherPourModifier() {
        String query = controller.getModifySearchField().getText().trim();
        if (query.isBlank()) return;
        try {
            service().getData().stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(query.toLowerCase()))
                    .findFirst()
                    .ifPresentOrElse(
                            this::ouvrirModifier,
                            () -> OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Introuvable",
                                    "Aucun événement trouvé pour : " + query));
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void modifierImage() {
        File file = choisirImage();
        if (file != null) {
            controller.setModifyImagePath(file.getAbsolutePath());
            controller.getModifyImageLabel().setText(file.getName());
            controller.getModifyImageLabel().setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #333; -fx-background-color: #f5f5f5; "
                    + "-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #c8c8c8; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 5;");
        }
    }

    void modifierEvenement() {
        if (controller.getSelectedEvenementId() == -1) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Aucun événement sélectionné",
                    "Cliquez sur ✏ dans la liste ou cherchez un événement d'abord.");
            return;
        }

        TextField modifyTitre = controller.getModifyTitre();
        TextArea modifyDescription = controller.getModifyDescription();
        TextField modifyOrganisateur = controller.getModifyOrganisateur();
        ComboBox<Type> modifyType = controller.getModifyType();
        DatePicker modifyDateDebut = controller.getModifyDateDebut();
        DatePicker modifyDateFin = controller.getModifyDateFin();
        TextField modifyTempsDebut = controller.getModifyTempsDebut();
        TextField modifyTempsFin = controller.getModifyTempsFin();
        TextField modifyNombrePlaces = controller.getModifyNombrePlaces();
        TextField modifyLieu = controller.getModifyLieu();
        TextField modifyAdresse = controller.getModifyAdresse();

        if (modifyTitre.getText().isBlank() || modifyDateDebut.getValue() == null
                || modifyDateFin.getValue() == null || modifyNombrePlaces.getText().isBlank()) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir : Titre, Dates et Nombre de places.");
            return;
        }
        if (modifyTitre.getText().trim().length() < 3) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Titre trop court",
                    "Le titre doit contenir au moins 3 caracteres.");
            return;
        }
        if (modifyTitre.getText().trim().length() > 100) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Titre trop long",
                    "Le titre ne doit pas depasser 100 caracteres.");
            return;
        }
        if (modifyType.getValue() == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Type manquant",
                    "Veuillez selectionner un type d'evenement.");
            return;
        }
        if (modifyDateFin.getValue().isBefore(modifyDateDebut.getValue())) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Dates invalides",
                    "La date de fin doit etre egale ou posterieure a la date de debut.");
            return;
        }
        LocalTime heureDebut = parseTime(modifyTempsDebut.getText());
        LocalTime heureFin   = parseTime(modifyTempsFin.getText());
        if (heureDebut == null || heureFin == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide",
                    "Format attendu : HH:mm (ex: 09:30)");
            return;
        }
        if (modifyDateDebut.getValue().equals(modifyDateFin.getValue()) && !heureFin.isAfter(heureDebut)) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide",
                    "L'horaire de fin doit etre apres l'horaire de debut lorsque les dates sont identiques.");
            return;
        }
        try {
            int nbPlaces = Integer.parseInt(modifyNombrePlaces.getText().trim());
            if (nbPlaces <= 0) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Places invalides",
                        "Le nombre de places doit etre superieur a 0.");
                return;
            }
            if (nbPlaces > 100000) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Places invalides",
                        "Le nombre de places ne peut pas depasser 100 000.");
                return;
            }
        } catch (NumberFormatException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Le nombre de places doit etre un entier valide.");
            return;
        }
        if (modifyOrganisateur.getText().isBlank()) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Champ manquant",
                    "Veuillez saisir le nom de l'organisateur.");
            return;
        }

        try {
            Evenement e = new Evenement();
            e.setTitre(modifyTitre.getText().trim());
            e.setDescription(modifyDescription.getText().trim());
            e.setOrganisateur(modifyOrganisateur.getText().trim());
            e.setTypeEvenement(modifyType.getValue());
            e.setDateDebut(modifyDateDebut.getValue());
            e.setDateFin(modifyDateFin.getValue());
            e.setHoraireDebut(heureDebut);
            e.setHoraireFin(heureFin);
            e.setCapaciteMax(Integer.parseInt(modifyNombrePlaces.getText().trim()));
            e.setPlacesDisponibles(Integer.parseInt(modifyNombrePlaces.getText().trim()));
            e.setLieu(modifyLieu.getText().trim());
            e.setAdresse(modifyAdresse.getText().trim());
            e.setImageUrl(controller.getModifyImagePath());
            e.setStatut(Statutevent.actif);

            service().updateEntity(controller.getSelectedEvenementId(), e);
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                    "Événement modifié avec succès !");
            controller.setSelectedEvenementId(-1);
            controller.chargerListe(null);
            controller.getMainTabPane().getSelectionModel().select(0);

        } catch (NumberFormatException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Le nombre de places doit être un entier.");
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur base de données",
                    ex.getMessage());
        }
    }

    // ============================================================
    //  ANNULER / SUPPRIMER
    // ============================================================

    void annulerEvenement(Evenement e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler l'événement ?");
        confirm.setContentText("Voulez-vous vraiment annuler \"" + e.getTitre() + "\" ?\n"
                + "Cette action changera le statut en « annulé ».");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service().updateStatut(e.getIdEvenement(), "annule");
                    OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                            "L'événement \"" + e.getTitre() + "\" a été annulé.");
                    controller.chargerListe(null);
                } catch (SQLException ex) {
                    OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    void supprimerEvenement(Evenement e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement ?");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + e.getTitre() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service().deleteEntity(e);
                    OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès",
                            "Événement supprimé !");
                    controller.chargerListe(null);
                } catch (SQLException ex) {
                    OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    // ============================================================
    //  UTILITAIRES
    // ============================================================

    private void clearCreateForm() {
        controller.getCreateTitre().clear();
        controller.getCreateDescription().clear();
        controller.getCreateOrganisateur().clear();
        controller.getCreateType().setValue(null);
        controller.getCreateDateDebut().setValue(null);
        controller.getCreateDateFin().setValue(null);
        controller.getCreateTempsDebut().clear();
        controller.getCreateTempsFin().clear();
        controller.getCreateNombrePlaces().clear();
        controller.getCreateLieu().clear();
        controller.getCreateAdresse().clear();
        controller.setCreateImagePath(null);
        controller.getCreateImageLabel().setText("Aucune image sélectionnée");
        controller.getCreateImageLabel().setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #888; -fx-background-color: #f5f5f5; "
                + "-fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #c8c8c8; "
                + "-fx-border-width: 1.5; -fx-border-radius: 5;");
    }

    private File choisirImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        return fc.showOpenDialog(controller.getBtnAccueil().getScene().getWindow());
    }

    private LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalTime.parse(text.trim(), timeFmt());
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }
}
