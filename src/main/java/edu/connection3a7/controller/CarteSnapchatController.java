package edu.connection3a7.controller;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.LocalisationTechnicienService;
import edu.connection3a7.tools.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import javafx.scene.control.*;
import netscape.javascript.JSObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class CarteSnapchatController {

    @FXML private WebView webView;
    @FXML private Label lblStatut;
    @FXML private Button btnPartagerPosition;

    private LocalisationTechnicienService locService;
    private int idTechnicienConnecte;
    private int idTechnicienCible;
    private String nomTechnicienCible;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {

        webView.getEngine().setJavaScriptEnabled(true);
        btnPartagerPosition.setVisible(false);
        btnPartagerPosition.setManaged(false);
        btnPartagerPosition.setText("🧭 Voir itinéraire");

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
        webView.getEngine().getLoadWorker().stateProperty()
                .addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            JSObject window = (JSObject) webView.getEngine().executeScript("window");
                            window.setMember("javaBridge", new JavaBridge());
                        } catch (Exception e) {
                            System.out.println("Impossible d'initialiser le bridge JavaScript");
                            e.printStackTrace();
                        }
                    }
                });

        locService = new LocalisationTechnicienService();
        idTechnicienConnecte = SessionManager.getInstance().getIdTechnicien();

        System.out.println("🗺️ Initialisation de la carte OpenStreetMap");

        chargerCarte();
        btnPartagerPosition.setOnAction(e -> voirItineraire());
    }
    public void setIdTechnicien(int id) {
        if (id > 0) {
            this.idTechnicienConnecte = id;
            System.out.println("ID technicien recu: " + id);
        }
    }
    public void setTechnicienCible(int id, String nom) {
        if (id > 0) {
            this.idTechnicienCible = id;
            this.nomTechnicienCible = nom;
        }
    }

    private void voirItineraire() {
        if (idTechnicienCible <= 0) {
            lblStatut.setText("⚠️ Sélectionnez un technicien avant d'ouvrir la carte");
            return;
        }

        try {
            CoordonneeDepart depart = recupererPositionDepuisIp();
            if (depart != null) {
                webView.getEngine().executeScript(String.format(
                        Locale.US,
                        "showRouteFromJavaStart(%f, %f);",
                        depart.latitude(),
                        depart.longitude()
                ));
            } else {
                webView.getEngine().executeScript("showRouteToSelected();");
            }
            String nom = (nomTechnicienCible == null || nomTechnicienCible.isBlank()) ? "#" + idTechnicienCible : nomTechnicienCible;
            lblStatut.setText("🧭 Calcul de l'itinéraire vers " + nom + "...");
        } catch (Exception e) {
            lblStatut.setText("⚠️ Carte non prête, cliquez Actualiser puis réessayez");
        }
    }

    private void chargerCarte() {
        try {
            List<LocalisationTechnicienService.PositionTechnicien> positions =
                    locService.getTechniciensAvecPosition();

            String html = genererHTMLCarte(positions);
            webView.getEngine().loadContent(html);
            if (idTechnicienCible > 0) {
                String nom = (nomTechnicienCible == null || nomTechnicienCible.isBlank()) ? "#" + idTechnicienCible : nomTechnicienCible;
                lblStatut.setText("👥 " + positions.size() + " technicien(s) en ligne | Cible: " + nom);
            } else {
                lblStatut.setText("👥 " + positions.size() + " technicien(s) en ligne");
            }

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
            String nomComplet = (pos.getTechnicien().getPrenom() + " " + pos.getTechnicien().getNom())
                    .replace("'", "\\'");

            markers.append(String.format(Locale.US, """
    var marker = L.marker([%f, %f], {
        icon: L.icon({
            iconUrl: '%s',
            iconSize: [25, 41]
        })
    }).addTo(map);

    marker.bindPopup("%s");
    marker.on('click', function () {
        selectedTarget = {
            id: %d,
            name: '%s',
            lat: %f,
            lng: %f
        };
        if (window.javaBridge && window.javaBridge.onTechnicienSelected) {
            window.javaBridge.onTechnicienSelected(%d, '%s');
        }
    });
    bounds.push([%f, %f]);
    """,
                    pos.getLatitude(),
                    pos.getLongitude(),
                    icone,
                    info.replace("\"", "\\\""),
                    pos.getTechnicien().getId_tech(),
                    nomComplet,
                    pos.getLatitude(),
                    pos.getLongitude(),
                    pos.getTechnicien().getId_tech(),
                    nomComplet,
                    pos.getLatitude(),
                    pos.getLongitude()
            ));
        }

        return String.format(Locale.US, """
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
                    window.onerror = function(msg, src, line, col) {
                        alert('JS ERROR: ' + msg + ' @' + line + ':' + col);
                    };
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
                    var bounds = [];
                    var selectedTarget = null;
                    var routeLayer = null;
                    var userMarker = null;
 
                    %s

                    if (bounds.length === 1) {
                        map.setView(bounds[0], 14);
                    } else if (bounds.length > 1) {
                        map.fitBounds(bounds, { padding: [30, 30] });
                    }

                    function drawRouteFromStart(myLat, myLng) {
                        if (!selectedTarget) {
                            alert("Aucun technicien sélectionné ou le technicien n'a pas de position active.");
                            return;
                        }

                        if (userMarker) {
                            map.removeLayer(userMarker);
                        }
                        userMarker = L.circleMarker([myLat, myLng], {
                            radius: 8,
                            color: '#1d4ed8',
                            fillColor: '#3b82f6',
                            fillOpacity: 0.9
                        }).addTo(map).bindPopup("Ma position");

                        var routeUrl = "https://router.project-osrm.org/route/v1/driving/"
                            + myLng + "," + myLat + ";"
                            + selectedTarget.lng + "," + selectedTarget.lat
                            + "?overview=full&geometries=geojson";

                        fetch(routeUrl)
                            .then(function(response) { return response.json(); })
                            .then(function(data) {
                                if (!data.routes || data.routes.length === 0) {
                                    alert("Impossible de calculer un itinéraire vers ce technicien.");
                                    return;
                                }

                                var coords = data.routes[0].geometry.coordinates.map(function(c) {
                                    return [c[1], c[0]];
                                });

                                if (routeLayer) {
                                    map.removeLayer(routeLayer);
                                }

                                routeLayer = L.polyline(coords, {
                                    color: '#2563eb',
                                    weight: 5,
                                    opacity: 0.9
                                }).addTo(map);

                                map.fitBounds(routeLayer.getBounds(), { padding: [30, 30] });

                                var km = data.routes[0].distance / 1000.0;
                                var mins = Math.round(data.routes[0].duration / 60.0);
                                userMarker.openPopup();
                                alert("Itinéraire vers " + selectedTarget.name + "\\nDistance: " + km.toFixed(1) + " km\\nDurée estimée: " + mins + " min");
                            })
                            .catch(function(err) {
                                alert("Erreur lors du calcul de l'itinéraire: " + err);
                            });
                    }

                    function askManualStart() {
                        var latInput = prompt("Entrez votre latitude de départ (ex: 36.8065):", "36.8065");
                        if (latInput === null) return;
                        var lngInput = prompt("Entrez votre longitude de départ (ex: 10.1815):", "10.1815");
                        if (lngInput === null) return;

                        var myLat = Number(latInput);
                        var myLng = Number(lngInput);
                        if (Number.isNaN(myLat) || Number.isNaN(myLng)) {
                            alert("Coordonnées invalides.");
                            return;
                        }
                        drawRouteFromStart(myLat, myLng);
                    }

                    function showRouteFromJavaStart(lat, lng) {
                        drawRouteFromStart(Number(lat), Number(lng));
                    }

                    function showRouteToSelected() {
                        if (!navigator.geolocation) {
                            alert("Géolocalisation indisponible dans cette vue. Saisissez votre position.");
                            askManualStart();
                            return;
                        }

                        navigator.geolocation.getCurrentPosition(function(position) {
                            drawRouteFromStart(position.coords.latitude, position.coords.longitude);
                        }, function(error) {
                            alert("Géolocalisation refusée (" + error.message + "). Saisissez votre position.");
                            askManualStart();
                        }, {
                            enableHighAccuracy: true,
                            timeout: 10000
                        });
                    }
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

    public class JavaBridge {
        public void onTechnicienSelected(int idTech, String nomTech) {
            Platform.runLater(() -> {
                idTechnicienCible = idTech;
                nomTechnicienCible = nomTech;
                btnPartagerPosition.setVisible(true);
                btnPartagerPosition.setManaged(true);
                lblStatut.setText("✅ Technicien sélectionné: " + nomTech);
            });
        }
    }

    private CoordonneeDepart recupererPositionDepuisIp() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipwho.is/"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.path("success").asBoolean(false)) {
                return null;
            }

            double latitude = root.path("latitude").asDouble(Double.NaN);
            double longitude = root.path("longitude").asDouble(Double.NaN);
            if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
                return null;
            }

            return new CoordonneeDepart(latitude, longitude);
        } catch (Exception e) {
            System.out.println("Position Java indisponible (fallback JS/manual): " + e.getMessage());
            return null;
        }
    }

    private record CoordonneeDepart(double latitude, double longitude) {}
}