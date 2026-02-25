package marketplace.tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * MapPicker — Click-to-pick address using OpenStreetMap + Leaflet.
 *
 * Architecture (critical for reliability in JavaFX 17 WebView):
 *
 * JavaScript --> JavaBridge.lookupAddress(lat, lon)
 * |
 * Java Thread
 * |
 * HttpURLConnection --> Nominatim API
 * |
 * Platform.runLater()
 * |
 * TextField.setText()
 *
 * WHY: JavaFX 17's WebKit blocks all outgoing XHR/fetch() network requests
 * in certain security configurations. Moving the HTTP call to Java-side
 * HttpURLConnection completely bypasses WebView network restrictions.
 */
public class MapPicker {

    // ── Static cache — WebView created once, reused ──────────────────────────
    private static WebView cachedWebView;
    private static WebEngine cachedWebEngine;

    /**
     * Static bridge reference — MUST be static to prevent GC.
     * JavaFX WebView holds only a weak reference to JS-registered objects.
     */
    private static JavaBridge currentBridge;

    // ── Result DTO ───────────────────────────────────────────────────────────

    public static class AddressResult {
        private final String address;
        private final String city;
        private final boolean confirmed;

        public AddressResult(String address, String city, boolean confirmed) {
            this.address = address;
            this.city = city;
            this.confirmed = confirmed;
        }

        public String getAddress() {
            return address;
        }

        public String getCity() {
            return city;
        }

        public boolean isConfirmed() {
            return confirmed;
        }
    }

    // ── JS→Java Bridge ───────────────────────────────────────────────────────

    /**
     * PUBLIC STATIC — required for JavaFX WebView reflection.
     * JavaScript calls lookupAddress(lat, lon); Java handles the HTTP call.
     */
    public static class JavaBridge {
        private volatile TextField addressField;
        private volatile TextField cityField;
        private volatile WebEngine engine;

        public void attach(TextField addr, TextField city, WebEngine eng) {
            this.addressField = addr;
            this.cityField = city;
            this.engine = eng;
        }

        /**
         * Called by JavaScript with the clicked lat/lon.
         * Java performs the Nominatim HTTP request on a background thread —
         * completely bypasses WebView network restrictions.
         */
        public void lookupAddress(double lat, double lon) {
            new Thread(() -> {
                try {
                    String urlStr = "https://nominatim.openstreetmap.org/reverse"
                            + "?format=json&lat=" + lat + "&lon=" + lon
                            + "&zoom=18&addressdetails=1";

                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    conn.setRequestProperty("User-Agent", "MarketplaceApp/1.0");
                    conn.setRequestProperty("Accept-Language", "fr");
                    conn.setRequestProperty("Accept", "application/json");

                    int status = conn.getResponseCode();
                    if (status == 200) {
                        StringBuilder sb = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                            String line;
                            while ((line = br.readLine()) != null)
                                sb.append(line);
                        }

                        // Parse JSON with Gson (already in project)
                        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
                        String displayName = root.has("display_name")
                                ? root.get("display_name").getAsString()
                                : "";

                        String city = "";
                        if (root.has("address")) {
                            JsonObject addr = root.getAsJsonObject("address");
                            if (addr.has("city"))
                                city = addr.get("city").getAsString();
                            else if (addr.has("town"))
                                city = addr.get("town").getAsString();
                            else if (addr.has("village"))
                                city = addr.get("village").getAsString();
                            else if (addr.has("county"))
                                city = addr.get("county").getAsString();
                            else if (addr.has("state"))
                                city = addr.get("state").getAsString();
                        }

                        final String finalAddr = displayName;
                        final String finalCity = city;

                        // Update TextFields on JavaFX thread
                        Platform.runLater(() -> {
                            if (addressField != null && !finalAddr.isEmpty())
                                addressField.setText(finalAddr);
                            if (cityField != null && !finalCity.isEmpty())
                                cityField.setText(finalCity);
                        });

                        // Update the map popup via JS
                        final String popupText = finalAddr.replace("'", "\\'");
                        final WebEngine eng = engine;
                        Platform.runLater(() -> {
                            if (eng != null) {
                                try {
                                    eng.executeScript(
                                            "if(window.updatePopup) updatePopup('" + popupText + "');");
                                } catch (Exception ignored) {
                                }
                            }
                        });

                    } else {
                        setError("Nominatim HTTP " + status);
                    }
                } catch (Exception ex) {
                    setError("Erreur réseau: " + ex.getMessage());
                }
            }, "nominatim-geocoder").start();
        }

        private void setError(String msg) {
            final WebEngine eng = engine;
            Platform.runLater(() -> {
                if (eng != null) {
                    try {
                        eng.executeScript("if(window.showStatus) showStatus('⚠ " +
                                msg.replace("'", "\\'") + "');");
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    // ── Main API ─────────────────────────────────────────────────────────────

    public AddressResult showAndWait(Stage ownerStage,
            String initialAddress,
            String initialCity) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(ownerStage);
        dialog.setTitle("Sélectionner une adresse sur la carte");
        dialog.setWidth(920);
        dialog.setHeight(700);

        // ── Text fields ──────────────────────────────────────────────────────
        TextField addressField = new TextField();
        addressField.setPromptText("Cliquez sur la carte pour remplir...");
        HBox.setHgrow(addressField, Priority.ALWAYS);
        if (initialAddress != null && !initialAddress.isEmpty())
            addressField.setText(initialAddress);

        TextField cityField = new TextField();
        cityField.setPromptText("Ville...");
        cityField.setPrefWidth(260);
        if (initialCity != null && !initialCity.isEmpty())
            cityField.setText(initialCity);

        // ── Init WebView + Bridge ────────────────────────────────────────────
        ensureWebViewCreated();

        if (currentBridge == null)
            currentBridge = new JavaBridge();
        currentBridge.attach(addressField, cityField, cachedWebEngine);

        if (cachedWebEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            registerBridge();
        }

        // ── Layout ───────────────────────────────────────────────────────────
        VBox top = new VBox(6);
        top.setPadding(new Insets(12));
        top.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        Label title = new Label("📍 Sélectionner une adresse");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label hint = new Label("Cliquez sur la carte pour sélectionner un emplacement.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        top.getChildren().addAll(title, hint);

        boolean[] confirmed = { false };

        Button confirmBtn = new Button("✅ Confirmer");
        confirmBtn.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 7 18; -fx-cursor: hand; -fx-background-radius: 5;");
        confirmBtn.setOnAction(e -> {
            confirmed[0] = true;
            dialog.close();
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 7 18; -fx-cursor: hand; -fx-background-radius: 5;");
        cancelBtn.setOnAction(e -> {
            confirmed[0] = false;
            dialog.close();
        });

        HBox btnRow = new HBox(10, cancelBtn, confirmBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        HBox addrRow = hrow(bold("Adresse:", 75), addressField);
        HBox cityRow = hrow(bold("Ville:", 75), cityField);

        VBox bottom = new VBox(10, addrRow, cityRow, btnRow);
        bottom.setPadding(new Insets(12));
        bottom.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        root.setTop(top);
        root.setCenter(cachedWebView);
        root.setBottom(bottom);

        dialog.setScene(new Scene(root));
        dialog.setOnShown(e -> {
            try {
                cachedWebEngine.executeScript("if(typeof resetMap==='function') resetMap();");
            } catch (Exception ignored) {
            }
        });

        dialog.showAndWait();

        root.setCenter(null);
        currentBridge.attach(null, null, null);

        return new AddressResult(addressField.getText(), cityField.getText(), confirmed[0]);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static void ensureWebViewCreated() {
        if (cachedWebView != null)
            return;

        cachedWebView = new WebView();
        cachedWebView.setContextMenuEnabled(false);
        cachedWebEngine = cachedWebView.getEngine();
        cachedWebEngine.setJavaScriptEnabled(true);
        cachedWebEngine.loadContent(buildMapHtml());

        cachedWebEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED)
                registerBridge();
        });
    }

    private static void registerBridge() {
        try {
            JSObject win = (JSObject) cachedWebEngine.executeScript("window");
            win.setMember("javaApp", currentBridge);
        } catch (Exception e) {
            System.err.println("[MapPicker] bridge registration error: " + e.getMessage());
        }
    }

    private static String buildMapHtml() {
        String leafJs = localUrl("/marketplace/GUI/leaflet/leaflet.js");
        String leafCss = localUrl("/marketplace/GUI/leaflet/leaflet.css");
        if (leafJs == null)
            leafJs = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
        if (leafCss == null)
            leafCss = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";

        final double lat = 36.8065, lon = 10.1815;
        final int zoom = 12;

        return "<!DOCTYPE html><html><head>"
                + "<meta charset='utf-8'>"
                + "<link rel='stylesheet' href='" + leafCss + "'>"
                + "<script src='" + leafJs + "'></script>"
                + "<style>"
                + "html,body{margin:0;padding:0;height:100%;}"
                + "#map{width:100%;height:100vh;}"
                + "#status{"
                + "position:fixed;bottom:0;left:0;right:0;"
                + "background:rgba(0,0,0,.7);color:#fff;"
                + "font:13px/1.5 sans-serif;padding:6px 14px;"
                + "z-index:9999;pointer-events:none;display:none;"
                + "}"
                + "</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<div id='status'></div>"
                + "<script>"

                // Map init — HTTP tiles (avoids SSL issues in JavaFX 17 WebKit)
                + "var map=L.map('map',{preferCanvas:true,updateWhenZooming:false,updateWhenIdle:true})"
                + ".setView([" + lat + "," + lon + "]," + zoom + ");"
                + "L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{"
                + "attribution:'© OpenStreetMap contributors',"
                + "maxZoom:19,keepBuffer:4,updateWhenZooming:false"
                + "}).addTo(map);"

                // State
                + "var marker=null,statusEl=document.getElementById('status');"
                + "function showStatus(m){statusEl.textContent=m;statusEl.style.display='block';}"
                + "function hideStatus(){statusEl.style.display='none';}"
                + "function resetMap(){"
                + "if(marker){map.removeLayer(marker);marker=null;}"
                + "map.setView([" + lat + "," + lon + "]," + zoom + ");"
                + "hideStatus();"
                + "}"

                // Called by Java (Platform.runLater) to update the popup after geocoding
                + "function updatePopup(addr){"
                + "if(marker) marker.bindPopup('<b>'+addr+'</b>').openPopup();"
                + "hideStatus();"
                + "}"

                // Click: drop marker, show "Searching...", call Java bridge
                // Java handles the HTTP request — bypasses WebView network restrictions
                + "map.on('click',function(e){"
                + "var lat=e.latlng.lat,lon=e.latlng.lng;"
                + "if(marker)map.removeLayer(marker);"
                + "marker=L.marker([lat,lon]).addTo(map);"
                + "marker.bindPopup('Recherche\u2026').openPopup();"
                + "showStatus('Recherche de l\\'adresse...');"
                + "try{"
                + "javaApp.lookupAddress(lat,lon);" // Java makes the HTTP call
                + "}catch(ex){"
                + "showStatus('Bridge indisponible: '+ex);"
                + "}"
                + "});"

                + "</script></body></html>";
    }

    private static String localUrl(String path) {
        try {
            URL u = MapPicker.class.getResource(path);
            return (u != null) ? u.toExternalForm() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Label bold(String text, double minWidth) {
        Label l = new Label(text);
        l.setMinWidth(minWidth);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private static HBox hrow(javafx.scene.Node... nodes) {
        HBox b = new HBox(10, nodes);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }
}
