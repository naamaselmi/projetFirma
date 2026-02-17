package edu.connection3a7.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Méthodes utilitaires UI partagées entre les contrôleurs Front et Evenement.
 */
public final class OutilsInterfaceGraphique {

    private OutilsInterfaceGraphique() { /* Classe utilitaire, pas d'instanciation */ }

    // ── Badges & Chips ──

    public static Label makeBadge(String text, String bg, String fg) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 3 10;"
        );
        return lbl;
    }

    public static HBox makeChip(String label, String value, String color) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        chip.getChildren().addAll(lbl, val);
        return chip;
    }

    // ── Form Fields ──

    public static TextField makeField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setPrefHeight(40);
        tf.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 0 12;"
        );
        return tf;
    }

    // ── Info Lines & Separators ──

    public static HBox ligneInfo(String label, String valeur) {
        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(10, 0, 10, 0));
        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        lbl.setMinWidth(130);
        Label val = new Label(valeur == null || valeur.isBlank() ? "-" : valeur);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #222;");
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);
        ligne.getChildren().addAll(lbl, val);
        return ligne;
    }

    public static Region sep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: #f0f0f0;");
        return r;
    }

    // ── Placeholders ──

    public static Label makePlaceholderLabel() {
        Label lbl = new Label("Image");
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        return lbl;
    }

    // ── Null Safety ──

    public static String nullSafe(Object o) {
        return o == null ? "" : o.toString();
    }

    // ── Alerts ──

    public static void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Google Maps ──

    public static void ouvrirMaps(String lieu, String adresse) {
        try {
            String q = (lieu != null && !lieu.isBlank() && adresse != null && !adresse.isBlank())
                    ? lieu + ", " + adresse
                    : (lieu != null && !lieu.isBlank() ? lieu : adresse);
            Desktop.getDesktop().browse(new URI(
                    "https://www.google.com/maps/search/?api=1&query="
                            + URLEncoder.encode(q, StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            System.err.println("Erreur Maps : " + ex.getMessage());
        }
    }
}
