package Firma.tools.GestionEvenement;

import Firma.entities.GestionEvenement.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import Firma.services.GestionEvenement.AccompagnantService;
import Firma.services.GestionEvenement.EvenementService;
import Firma.services.GestionEvenement.ParticipationService;
import Firma.services.GestionEvenement.UtilisateurService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Serveur HTTP embarqué léger pour afficher les tickets de participation
 * sur mobile via QR Code.
 *
 * Utilise com.sun.net.httpserver (intégré au JDK, zéro dépendance).
 * Accessible depuis les appareils sur le même réseau Wi-Fi.
 */
public class TicketServerService {

    private static TicketServerService instance;
    private HttpServer server;
    private int port = 8642;
    private String lanAddress;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();
    private final AccompagnantService accompagnantService = new AccompagnantService();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private TicketServerService() {}

    public static synchronized TicketServerService getInstance() {
        if (instance == null) {
            instance = new TicketServerService();
        }
        return instance;
    }

    /**
     * Démarre le serveur HTTP sur le réseau local.
     * Tente d'abord de libérer le port principal (8642) si un ancien
     * processus Java le bloque encore, puis se rabat sur le port alternatif.
     */
    public void demarrer() {
        if (server != null) return; // Déjà démarré

        lanAddress = detecterAdresseIP();

        // Essayer le port principal — si occupé, tenter de libérer
        if (!tryBind(port)) {
            System.out.println("Port " + port + " occupé, tentative de libération...");
            libererPort(port);

            // Petit délai pour laisser l'OS libérer le socket
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}

            if (!tryBind(port)) {
                // Toujours occupé → port alternatif
                port = 8643;
                if (!tryBind(port)) {
                    System.err.println("Échec du démarrage du serveur de tickets sur les ports 8642 et 8643.");
                    return;
                }
            }
        }

        System.out.println("Serveur de tickets démarré sur http://" + lanAddress + ":" + port + "/ticket");
        ouvrirPortFirewall();
    }

    /**
     * Tente de démarrer le HttpServer sur le port donné.
     * @return true si le serveur a démarré avec succès
     */
    private boolean tryBind(int portToBind) {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", portToBind), 0);
            server.createContext("/ticket", new TicketHandler());
            server.createContext("/confirm", new ConfirmHandler());
            server.setExecutor(null);
            server.start();
            return true;
        } catch (IOException e) {
            server = null;
            return false;
        }
    }

    /**
     * Tente de tuer un ancien processus Java/javaw qui bloque le port
     * (typiquement un run précédent d'IntelliJ qui n'a pas été arrêté proprement).
     */
    private void libererPort(int portToFree) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("windows")) return;

        try {
            // Chercher le PID qui écoute sur le port
            Process netstat = new ProcessBuilder("cmd", "/c",
                    "netstat -aon | findstr :" + portToFree + " | findstr LISTENING")
                    .redirectErrorStream(true).start();
            String output = new String(netstat.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            netstat.waitFor(5, TimeUnit.SECONDS);

            long myPid = ProcessHandle.current().pid();
            for (String line : output.split("\n")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    try {
                        long pid = Long.parseLong(parts[parts.length - 1].trim());
                        if (pid != myPid && pid > 0) {
                            System.out.println("Ancien processus détecté sur le port " + portToFree
                                    + " (PID " + pid + "), arrêt en cours...");
                            Process kill = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid))
                                    .redirectErrorStream(true).start();
                            kill.waitFor(5, TimeUnit.SECONDS);
                            if (kill.exitValue() == 0) {
                                System.out.println("✓ Ancien processus (PID " + pid + ") arrêté.");
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible de libérer le port " + portToFree + " : " + e.getMessage());
        }
    }

    /**
     * Tente d'ouvrir le port dans le pare-feu Windows pour permettre
     * aux appareils mobiles du réseau local de scanner les QR codes.
     * Ouvre les deux ports (principal et alternatif) pour couvrir tous les cas.
     */
    private void ouvrirPortFirewall() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("windows")) return;

        // Supprimer l'ancienne règle (ignore les erreurs) puis recréer avec les bons ports
        try {
            new ProcessBuilder("netsh", "advfirewall", "firewall", "delete", "rule",
                    "name=Firma Ticket Server")
                    .redirectErrorStream(true).start().waitFor(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "netsh", "advfirewall", "firewall", "add", "rule",
                    "name=Firma Ticket Server", "dir=in", "action=allow",
                    "protocol=tcp", "localport=8642,8643", "profile=any"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (finished && p.exitValue() == 0) {
                System.out.println("✓ Règle pare-feu ajoutée (ports 8642-8643)");
            } else {
                printFirewallHelp();
            }
        } catch (Exception e) {
            printFirewallHelp();
        }
    }

    private void printFirewallHelp() {
        System.out.println("⚠ IMPORTANT — Pour que le QR Code fonctionne sur mobile :");
        System.out.println("  Exécutez UNE FOIS en tant qu'administrateur dans PowerShell :");
        System.out.println("  netsh advfirewall firewall add rule name=\"Firma Ticket Server\" " +
                "dir=in action=allow protocol=tcp localport=8642,8643 profile=any");
        System.out.println("  (Clic droit sur PowerShell → Exécuter en tant qu'administrateur)");
    }

    /**
     * Arrête le serveur HTTP.
     */
    public void arreter() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Serveur de tickets arrêté.");
        }
    }

    /**
     * Construit l'URL du ticket accessible depuis le réseau local.
     */
    public String getTicketURL(String codeParticipation) {
        if (lanAddress == null) {
            lanAddress = detecterAdresseIP();
        }
        return "http://" + lanAddress + ":" + port + "/ticket?code=" +
                URLEncoder.encode(codeParticipation, StandardCharsets.UTF_8);
    }

    public boolean isRunning() {
        return server != null;
    }

    public String getLanAddress() {
        if (lanAddress == null) lanAddress = detecterAdresseIP();
        return lanAddress;
    }

    public int getPort() {
        return port;
    }

    /**
     * Détecte l'adresse IP du réseau local (Wi-Fi/Ethernet).
     */
    private String detecterAdresseIP() {
        try {
            String bestAddress = null;

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;

                // Ignorer les interfaces virtuelles (VirtualBox, VMware, Docker, etc.)
                String name = ni.getDisplayName().toLowerCase();
                String niName = ni.getName().toLowerCase();
                boolean isVirtual = ni.isVirtual()
                        || name.contains("virtualbox") || name.contains("vmware")
                        || name.contains("vmnet") || name.contains("vbox")
                        || name.contains("docker") || name.contains("hyper-v")
                        || name.contains("virtual") || name.contains("vpn")
                        || niName.contains("vbox") || niName.contains("vmnet")
                        || niName.contains("docker");

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();

                        // Ignorer les plages IP virtuelles ou link-local
                        if (ip.startsWith("192.168.56.") || ip.startsWith("172.17.")
                                || ip.startsWith("169.254.")    // APIPA / link-local
                                || ip.startsWith("192.168.133.") // VMnet8
                                || ip.startsWith("192.168.217.") // VMnet1
                        ) {
                            continue;
                        }

                        if (!isVirtual) {
                            // Interface réelle trouvée → la prioriser
                            System.out.println("IP réseau détectée (" + ni.getDisplayName() + ") : " + ip);
                            return ip;
                        }

                        // Garder comme fallback si aucune interface réelle n'est trouvée
                        if (bestAddress == null) {
                            bestAddress = ip;
                        }
                    }
                }
            }

            if (bestAddress != null) {
                System.out.println("IP fallback détectée : " + bestAddress);
                return bestAddress;
            }
        } catch (SocketException e) {
            System.err.println("Erreur détection IP : " + e.getMessage());
        }
        return "localhost";
    }

    // ================================================================
    //  Handler HTTP : sert la page ticket
    // ================================================================

    // ================================================================
    //  Handler HTTP : confirmation de participation par email
    // ================================================================

    private class ConfirmHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("[TicketServer] Requête confirmation reçue: " + exchange.getRequestURI()
                    + " de " + exchange.getRemoteAddress());

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");

            String query = exchange.getRequestURI().getQuery();
            String code = extraireParametre(query, "code");

            String html;
            if (code == null || code.isBlank()) {
                html = genererPageErreur("Code manquant", "Lien de confirmation invalide.");
            } else {
                html = traiterConfirmation(code);
            }

            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    /**
     * Traite la confirmation : change le statut EN_ATTENTE → CONFIRME
     * et envoie le ticket PDF par email.
     */
    private String traiterConfirmation(String code) {
        try {
            Participation participation = participationService.getByCode(code);
            if (participation == null) {
                return genererPageErreur("Participation introuvable",
                        "Aucune participation trouvée pour le code : " + escapeHtml(code));
            }

            // Vérifier si déjà confirmé
            if (participation.getStatut() == Statut.CONFIRME
                    || participation.getStatut() == Statut.confirme) {
                return genererPageConfirmation("Déjà confirmé",
                        "Votre participation est déjà confirmée ! Vérifiez vos emails pour le ticket PDF.",
                        true);
            }

            // Mettre à jour le statut
            participationService.updateStatut(participation.getIdParticipation(),
                    Statut.CONFIRME);

            // Charger les données pour l'email avec ticket PDF
            Evenement evenement = evenementService.getById(participation.getIdEvenement());
            Utilisateur utilisateur = utilisateurService.getById(participation.getIdUtilisateur());
            List<Accompagnant> accompagnants = accompagnantService.getByParticipation(
                    participation.getIdParticipation());

            if (utilisateur != null && utilisateur.getEmail() != null) {
                // Mettre à jour la participation en mémoire
                participation.setStatut(Statut.CONFIRME);

                // Envoyer le ticket PDF par email (asynchrone)
                EmailService.getInstance().envoyerEmailTicketPDF(
                        utilisateur.getEmail(),
                        utilisateur.getPrenom(),
                        utilisateur.getNom(),
                        evenement,
                        participation,
                        accompagnants
                ).thenAccept(success -> {
                    if (success) {
                        System.out.println("Ticket PDF envoyé par email à " + utilisateur.getEmail());
                    } else {
                        System.err.println("Échec envoi ticket PDF à " + utilisateur.getEmail());
                    }
                });
            }

            return genererPageConfirmation("Participation confirmée !",
                    "Merci " + safe(utilisateur != null ? utilisateur.getPrenom() : "") 
                    + " ! Votre participation à <strong>" + escapeHtml(evenement != null ? evenement.getTitre() : "") 
                    + "</strong> est maintenant confirmée.<br><br>"
                    + "📧 Un email avec votre ticket PDF va vous être envoyé sous peu.",
                    false);

        } catch (Exception e) {
            e.printStackTrace();
            return genererPageErreur("Erreur",
                    "Une erreur est survenue lors de la confirmation : " + escapeHtml(e.getMessage()));
        }
    }

    private String genererPageConfirmation(String titre, String message, boolean dejaConfirme) {
        String bgColor = dejaConfirme ? "#fff8e1" : "#e8f5e2";
        String iconEmoji = dejaConfirme ? "ℹ️" : "🎉";
        String titleColor = dejaConfirme ? "#f59e0b" : "#49ad32";

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Confirmation — Firma</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, %s 0%%, #fefbde 100%%);
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 20px;
                    }
                    .card {
                        background: white;
                        border-radius: 20px;
                        max-width: 440px;
                        width: 100%%;
                        padding: 45px 35px;
                        text-align: center;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 56px; margin-bottom: 18px; }
                    .title { font-size: 22px; font-weight: 700; color: %s; margin-bottom: 14px; }
                    .msg { font-size: 15px; color: #555; line-height: 1.6; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">%s</div>
                    <div class="title">%s</div>
                    <div class="msg">%s</div>
                </div>
            </body>
            </html>
            """.formatted(bgColor, titleColor, iconEmoji, titre, message);
    }

    // ================================================================
    //  Handler HTTP : sert la page ticket
    // ================================================================

    private class TicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("[TicketServer] Requête ticket reçue: " + exchange.getRequestURI()
                    + " de " + exchange.getRemoteAddress());

            // CORS headers pour compatibilité
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");

            String query = exchange.getRequestURI().getQuery();
            String code = extraireParametre(query, "code");

            String html;
            if (code == null || code.isBlank()) {
                html = genererPageErreur("Code de participation manquant",
                        "Veuillez scanner un QR Code de ticket valide.");
            } else {
                html = genererPageTicket(code);
            }

            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    private String extraireParametre(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    // ================================================================
    //  Génération de la page HTML du ticket
    // ================================================================

    private String genererPageTicket(String code) {
        try {
            // Déterminer si c'est un accompagnant (code contient -A1, -A2, etc.)
            String baseCode = code;
            String accompagnantSuffix = null;
            if (code.matches(".*-A\\d+$")) {
                int lastDash = code.lastIndexOf("-A");
                baseCode = code.substring(0, lastDash);
                accompagnantSuffix = code.substring(lastDash + 2); // ex: "1", "2"
            }

            // Chercher la participation par code
            Participation participation = participationService.getByCode(baseCode);
            if (participation == null) {
                return genererPageErreur("Ticket introuvable",
                        "Aucune participation trouvée pour le code : " + escapeHtml(code));
            }

            // Charger l'événement
            Evenement evenement = evenementService.getById(participation.getIdEvenement());
            if (evenement == null) {
                return genererPageErreur("Événement introuvable",
                        "L'événement associé à ce ticket n'existe plus.");
            }

            // Charger l'utilisateur
            Utilisateur utilisateur = utilisateurService.getById(participation.getIdUtilisateur());

            // Nom du participant
            String nomParticipant;
            String roleParticipant;

            if (accompagnantSuffix != null) {
                // C'est un ticket d'accompagnant
                int indexAccomp = Integer.parseInt(accompagnantSuffix) - 1;
                List<Accompagnant> accompagnants = accompagnantService.getByParticipation(
                        participation.getIdParticipation());

                if (indexAccomp >= 0 && indexAccomp < accompagnants.size()) {
                    Accompagnant a = accompagnants.get(indexAccomp);
                    nomParticipant = safe(a.getPrenom()) + " " + safe(a.getNom());
                    roleParticipant = "Accompagnant " + (indexAccomp + 1);
                } else {
                    nomParticipant = "Accompagnant";
                    roleParticipant = "Accompagnant";
                }
            } else {
                nomParticipant = utilisateur != null
                        ? safe(utilisateur.getPrenom()) + " " + safe(utilisateur.getNom())
                        : "Participant";
                roleParticipant = "Participant principal";
            }

            // Construire les informations
            String dateStr = evenement.getDateDebut() != null
                    ? evenement.getDateDebut().format(DATE_FMT) : "-";
            if (evenement.getDateFin() != null && !evenement.getDateFin().equals(evenement.getDateDebut())) {
                dateStr += " → " + evenement.getDateFin().format(DATE_FMT);
            }

            String horaireStr = "";
            if (evenement.getHoraireDebut() != null) {
                horaireStr = evenement.getHoraireDebut().format(TIME_FMT);
                if (evenement.getHoraireFin() != null) {
                    horaireStr += " - " + evenement.getHoraireFin().format(TIME_FMT);
                }
            }

            String lieuStr = safe(evenement.getLieu());
            if (evenement.getAdresse() != null && !evenement.getAdresse().isBlank()) {
                lieuStr += (lieuStr.isEmpty() ? "" : ", ") + safe(evenement.getAdresse());
            }

            String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" +
                    URLEncoder.encode(lieuStr, StandardCharsets.UTF_8);

            String statut = participation.getStatut() != null
                    ? participation.getStatut().name() : "CONFIRME";

            return genererHTML(
                    escapeHtml(evenement.getTitre()),
                    escapeHtml(code),
                    escapeHtml(nomParticipant.trim()),
                    escapeHtml(roleParticipant),
                    escapeHtml(dateStr),
                    escapeHtml(horaireStr),
                    escapeHtml(lieuStr),
                    mapsUrl,
                    escapeHtml(safe(evenement.getOrganisateur())),
                    statut,
                    escapeHtml(evenement.getTypeEvenement() != null
                            ? evenement.getTypeEvenement().name() : "")
            );

        } catch (Exception e) {
            e.printStackTrace();
            return genererPageErreur("Erreur",
                    "Une erreur est survenue lors du chargement du ticket : " + escapeHtml(e.getMessage()));
        }
    }

    private String genererHTML(String titre, String code, String nom, String role,
                                String date, String horaire, String lieu, String mapsUrl,
                                String organisateur, String statut, String type) {
        String statutColor = statut.equalsIgnoreCase("CONFIRME") ? "#2d8a1a" : "#e74c3c";
        String statutBg = statut.equalsIgnoreCase("CONFIRME") ? "#e8f8e0" : "#fde8e8";

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ticket — %s</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #e8f5e2 0%%, #fefbde 100%%);
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: flex-start;
                        padding: 20px;
                    }
                    .ticket {
                        background: white;
                        border-radius: 20px;
                        max-width: 420px;
                        width: 100%%;
                        overflow: hidden;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.12);
                    }
                    .ticket-header {
                        background: linear-gradient(135deg, #49ad32 0%%, #3a8c28 100%%);
                        color: white;
                        padding: 28px 24px 22px;
                        text-align: center;
                    }
                    .ticket-header .logo {
                        font-size: 14px;
                        font-weight: 700;
                        letter-spacing: 3px;
                        opacity: 0.8;
                        margin-bottom: 8px;
                    }
                    .ticket-header h1 {
                        font-size: 22px;
                        font-weight: 700;
                        line-height: 1.3;
                        margin-bottom: 6px;
                    }
                    .ticket-header .type-badge {
                        display: inline-block;
                        background: rgba(255,255,255,0.2);
                        padding: 4px 14px;
                        border-radius: 20px;
                        font-size: 12px;
                        font-weight: 600;
                    }
                    .ticket-divider {
                        position: relative;
                        height: 20px;
                        background: white;
                    }
                    .ticket-divider::before,
                    .ticket-divider::after {
                        content: '';
                        position: absolute;
                        width: 20px;
                        height: 20px;
                        background: linear-gradient(135deg, #e8f5e2 0%%, #fefbde 100%%);
                        border-radius: 50%%;
                        top: -10px;
                    }
                    .ticket-divider::before { left: -10px; }
                    .ticket-divider::after { right: -10px; }
                    .ticket-divider .dashed {
                        position: absolute;
                        top: 50%%;
                        left: 20px;
                        right: 20px;
                        border-top: 2px dashed #e0dcc0;
                    }
                    .ticket-body { padding: 20px 24px; }
                    .participant-section {
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    .participant-section .role {
                        font-size: 11px;
                        color: #49ad32;
                        font-weight: 700;
                        letter-spacing: 1px;
                        text-transform: uppercase;
                    }
                    .participant-section .name {
                        font-size: 24px;
                        font-weight: 700;
                        color: #222;
                        margin-top: 4px;
                    }
                    .info-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 16px;
                        margin-bottom: 20px;
                    }
                    .info-item .label {
                        font-size: 10px;
                        color: #999;
                        font-weight: 700;
                        letter-spacing: 0.5px;
                        text-transform: uppercase;
                        margin-bottom: 4px;
                    }
                    .info-item .value {
                        font-size: 14px;
                        color: #333;
                        font-weight: 500;
                    }
                    .info-item.full-width {
                        grid-column: 1 / -1;
                    }
                    .lieu-link {
                        color: #49ad32;
                        text-decoration: none;
                        font-weight: 600;
                    }
                    .lieu-link:hover { text-decoration: underline; }
                    .code-section {
                        text-align: center;
                        padding: 16px;
                        background: #f9f8f0;
                        border-radius: 12px;
                        margin-bottom: 16px;
                    }
                    .code-section .code-label {
                        font-size: 10px;
                        color: #999;
                        font-weight: 700;
                        letter-spacing: 1px;
                    }
                    .code-section .code-value {
                        font-size: 22px;
                        font-weight: 800;
                        color: #49ad32;
                        letter-spacing: 2px;
                        margin-top: 4px;
                        font-family: 'Courier New', monospace;
                    }
                    .statut-badge {
                        display: block;
                        text-align: center;
                        padding: 10px;
                        border-radius: 10px;
                        font-size: 14px;
                        font-weight: 700;
                        letter-spacing: 1px;
                        background: %s;
                        color: %s;
                    }
                    .ticket-footer {
                        text-align: center;
                        padding: 16px 24px 24px;
                        color: #bbb;
                        font-size: 11px;
                    }
                    .ticket-footer .firma-brand {
                        font-weight: 700;
                        color: #49ad32;
                    }
                    .maps-btn {
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        margin-top: 8px;
                        padding: 10px 20px;
                        background: #49ad32;
                        color: white;
                        border-radius: 25px;
                        text-decoration: none;
                        font-size: 13px;
                        font-weight: 600;
                        transition: transform 0.2s;
                    }
                    .maps-btn:active { transform: scale(0.96); }
                </style>
            </head>
            <body>
                <div class="ticket">
                    <div class="ticket-header">
                        <div class="logo">FIRMA</div>
                        <h1>%s</h1>
                        <span class="type-badge">%s</span>
                    </div>

                    <div class="ticket-divider"><div class="dashed"></div></div>

                    <div class="ticket-body">
                        <div class="participant-section">
                            <div class="role">%s</div>
                            <div class="name">%s</div>
                        </div>

                        <div class="info-grid">
                            <div class="info-item">
                                <div class="label">📅 Date</div>
                                <div class="value">%s</div>
                            </div>
                            <div class="info-item">
                                <div class="label">🕐 Horaire</div>
                                <div class="value">%s</div>
                            </div>
                            <div class="info-item full-width">
                                <div class="label">📍 Lieu</div>
                                <div class="value"><a class="lieu-link" href="%s" target="_blank">%s</a></div>
                            </div>
                            <div class="info-item full-width">
                                <div class="label">🏢 Organisateur</div>
                                <div class="value">%s</div>
                            </div>
                        </div>

                        <div class="code-section">
                            <div class="code-label">CODE DE PARTICIPATION</div>
                            <div class="code-value">%s</div>
                        </div>

                        <div class="statut-badge">✓ %s</div>

                        <div style="text-align:center; margin-top: 14px;">
                            <a class="maps-btn" href="%s" target="_blank">
                                📍 Ouvrir dans Google Maps
                            </a>
                        </div>
                    </div>

                    <div class="ticket-footer">
                        Propulsé par <span class="firma-brand">FIRMA</span> — Gestion d'événements
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                titre,           // <title>
                statutBg,        // statut background
                statutColor,     // statut color
                titre,           // h1
                type,            // type badge
                role,            // role
                nom,             // name
                date,            // date
                horaire,         // horaire
                mapsUrl,         // lieu link href
                lieu,            // lieu text
                organisateur,    // organisateur
                code,            // code value
                statut,          // statut badge text
                mapsUrl          // maps button href
        );
    }

    private String genererPageErreur(String titre, String message) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Erreur — Firma</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #fde8e8 0%%, #fefbde 100%%);
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 20px;
                    }
                    .error-card {
                        background: white;
                        border-radius: 20px;
                        max-width: 400px;
                        width: 100%%;
                        padding: 40px 30px;
                        text-align: center;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                    }
                    .error-icon { font-size: 48px; margin-bottom: 16px; }
                    .error-title { font-size: 20px; font-weight: 700; color: #e74c3c; margin-bottom: 10px; }
                    .error-msg { font-size: 14px; color: #666; line-height: 1.5; }
                </style>
            </head>
            <body>
                <div class="error-card">
                    <div class="error-icon">⚠️</div>
                    <div class="error-title">%s</div>
                    <div class="error-msg">%s</div>
                </div>
            </body>
            </html>
            """.formatted(titre, message);
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
