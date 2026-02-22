package edu.connection3a7.controller;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.LocalisationTechnicienService;
import edu.connection3a7.tools.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.control.*;
import java.util.List;

public class CarteSnapchatController {

    @FXML private WebView webView;
    @FXML private Label lblStatut;
    @FXML private ToggleButton btnPartagerPosition;

    private LocalisationTechnicienService locService;
    private int idTechnicienConnecte;

    @FXML
    public void initialize() {

        webView.getEngine().setOnAlert(e ->
                System.out.println("JS ALERT: " + e.getData())

        );

        webView.getEngine().getLoadWorker().exceptionProperty()
                .addListener((obs, oldEx, ex) -> {
                    if (ex != null) {
                        System.out.println("WEBVIEW ERROR:");
                        ex.printStackTrace();
                    }
                });

        locService = new LocalisationTechnicienService();
        idTechnicienConnecte = SessionManager.getInstance().getIdTechnicien();

        System.out.println("🗺️ Initialisation de la carte OpenStreetMap");

        verifierStatutPartage();

        chargerCarte();
        btnPartagerPosition.setOnAction(e -> togglePartage());
    }
    public void setIdTechnicien(int id) {
        this.idTechnicienConnecte = id;
        System.out.println("ID technicien recu: " + id);
        verifierStatutPartage();
    }

    private void verifierStatutPartage() {
        try {
            List<LocalisationTechnicienService.PositionTechnicien> positions =
                    locService.getTechniciensAvecPosition();

            for (var pos : positions) {
                if (pos.getTechnicien().getId_tech() == idTechnicienConnecte) {
                    btnPartagerPosition.setSelected(true);
                    btnPartagerPosition.setText("📍 Partage activé");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void togglePartage() {
        boolean actif = btnPartagerPosition.isSelected();
        btnPartagerPosition.setText(actif ? "📍 Partage activé" : "🌍 Partager ma position");
        locService.activerPartage(idTechnicienConnecte, actif);

        if (actif) {
            locService.mettreAJourPosition(idTechnicienConnecte, 36.8065, 10.1815);
            chargerCarte();
        }
    }

    private void chargerCarte() {
        try {
            List<LocalisationTechnicienService.PositionTechnicien> positions =
                    locService.getTechniciensAvecPosition();

            String html = genererHTMLCarte(positions);
            webView.getEngine().loadContent(html);

            lblStatut.setText("👥 " + positions.size() + " technicien(s) en ligne");

        } catch (Exception e) {
            e.printStackTrace();
            webView.getEngine().loadContent(genererCarteSecours());
            lblStatut.setText("⚠️ Erreur de chargement");
        }
    }

    private String genererHTMLCarte(List<LocalisationTechnicienService.PositionTechnicien> positions) {
        StringBuilder markers = new StringBuilder();

        if (positions.isEmpty()) {
            return genererCarteVide();
        }

        for (var pos : positions) {
            // ✅ Utilisation de la méthode estActif() qui existe dans PositionTechnicien
            String icone = pos.estActif() ?
                    "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png" :
                    "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-orange.png";

            String info = String.format(
                    "<b>%s %s</b><br>%s<br><i>%s</i>",
                    pos.getTechnicien().getPrenom(),
                    pos.getTechnicien().getNom(),
                    pos.getTechnicien().getSpecialite(),
                    pos.getStatut()
            );

            markers.append(String.format("""
    var marker = L.marker([%f, %f], {
        icon: L.icon({
            iconUrl: '%s',
            iconSize: [25, 41]
        })
    }).addTo(map);

    marker.bindPopup("%s");
    """,
                    pos.getLatitude(),
                    pos.getLongitude(),
                    icone,
                    info.replace("\"", "\\\"")
            ));
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <script>
                    window.L_DISABLE_3D = true;
                    window.L_NO_TOUCH = true;
                </script>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body { height: 100%%; margin: 0; padding: 0; }
                    #map { height: 600px; width: 100%%; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map', {
                        zoomAnimation: false,
                        fadeAnimation: false,
                        markerZoomAnimation: false,
                        preferCanvas: true,
                        zoomSnap: 1
                    }).setView([36.8065, 10.1815], 12);
 
                    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap',
                        updateWhenZooming: false,
                        updateWhenIdle: true,
                        keepBuffer: 1
                    }).addTo(map);
 
                    setTimeout(function () { map.invalidateSize(true); }, 150);
 
                    %s
                </script>
            </body>
            </html>
            """, markers.toString());
    }

    private String genererCarteVide() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <script>
                    window.L_DISABLE_3D = true;
                    window.L_NO_TOUCH = true;
                </script>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body { height: 100%; margin: 0; padding: 0; }
                    #map { height: 600px; width: 100%; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map', {
                        zoomAnimation: false,
                        fadeAnimation: false,
                        markerZoomAnimation: false,
                        preferCanvas: true,
                        zoomSnap: 1
                    }).setView([36.8065, 10.1815], 12);
 
                    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap',
                        updateWhenZooming: false,
                        updateWhenIdle: true,
                        keepBuffer: 1
                    }).addTo(map);
 
                    setTimeout(function () { map.invalidateSize(true); }, 150);
 
                    L.marker([36.8065, 10.1815]).addTo(map)
                        .bindPopup('Tunis<br>Aucun technicien en ligne');
                </script>
            </body>
            </html>
            """;
    }

    private String genererCarteSecours() {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial; text-align: center; padding: 50px;">
                <h2 style="color: #1a961e;">🗺️ Carte OpenStreetMap</h2>
                <p>Chargement de la carte...</p>
                <p><small>Si la carte ne s'affiche pas, vérifie ta connexion internet.</small></p>
            </body>
            </html>
            """;
    }

    @FXML
    private void actualiser() {
        chargerCarte();
        System.out.println("🔄 Carte actualisée");
    }
}