package edu.connection3a7.service;

import java.util.*;

public class DiagnosticIAService {

    private static final Map<String, String> KEYWORDS_TO_TYPE = new HashMap<>();
    private static final Map<String, String> SOLUTIONS_CLIENT = new HashMap<>();
    private static final Map<String, String> URGENCE = new HashMap<>();
    private static final Map<String, String> TECHNICIEN_SUGGERE = new HashMap<>();

    static {
        // Mots-clés -> type de problème
        KEYWORDS_TO_TYPE.put("internet", "Réseau");
        KEYWORDS_TO_TYPE.put("wifi", "Réseau");
        KEYWORDS_TO_TYPE.put("connexion", "Réseau");
        KEYWORDS_TO_TYPE.put("routeur", "Réseau");
        KEYWORDS_TO_TYPE.put("modem", "Réseau");
        KEYWORDS_TO_TYPE.put("câble", "Réseau");

        KEYWORDS_TO_TYPE.put("lent", "Performance");
        KEYWORDS_TO_TYPE.put("rame", "Performance");
        KEYWORDS_TO_TYPE.put("bloque", "Performance");
        KEYWORDS_TO_TYPE.put("fige", "Performance");

        KEYWORDS_TO_TYPE.put("eau", "Plomberie");
        KEYWORDS_TO_TYPE.put("fuite", "Plomberie");
        KEYWORDS_TO_TYPE.put("robinet", "Plomberie");
        KEYWORDS_TO_TYPE.put("chasse", "Plomberie");
        KEYWORDS_TO_TYPE.put("inondation", "Plomberie");

        KEYWORDS_TO_TYPE.put("electricité", "Électricité");
        KEYWORDS_TO_TYPE.put("courant", "Électricité");
        KEYWORDS_TO_TYPE.put("prise", "Électricité");
        KEYWORDS_TO_TYPE.put("disjoncteur", "Électricité");
        KEYWORDS_TO_TYPE.put("lumière", "Électricité");

        KEYWORDS_TO_TYPE.put("chauffage", "Chauffage");
        KEYWORDS_TO_TYPE.put("chaudière", "Chauffage");
        KEYWORDS_TO_TYPE.put("radiateur", "Chauffage");
        KEYWORDS_TO_TYPE.put("froid", "Chauffage");

        // Solutions pour le client
        SOLUTIONS_CLIENT.put("Réseau",
                "🔧 Problème réseau - Solutions à essayer vous-même :\n" +
                        "• Redémarrez votre routeur (débranchez 30 secondes)\n" +
                        "• Vérifiez les câbles Ethernet\n" +
                        "• Testez avec un autre appareil\n" +
                        "• Contactez votre fournisseur d'accès si le problème persiste");

        SOLUTIONS_CLIENT.put("Performance",
                "⚡ Problème de performance - Solutions :\n" +
                        "• Fermez les applications inutiles\n" +
                        "• Redémarrez votre ordinateur\n" +
                        "• Vérifiez les mises à jour Windows\n" +
                        "• Libérez de l'espace disque");

        SOLUTIONS_CLIENT.put("Plomberie",
                "💧 Problème de plomberie - Que faire :\n" +
                        "• Coupez l'arrivée d'eau si fuite importante\n" +
                        "• Vérifiez les joints et robinets\n" +
                        "• Placez un seau sous la fuite\n" +
                        "• Un plombier interviendra rapidement");

        SOLUTIONS_CLIENT.put("Électricité",
                "⚡ Problème électrique - SÉCURITÉ D'ABORD :\n" +
                        "• Vérifiez le disjoncteur\n" +
                        "• Ne touchez pas les fils dénudés\n" +
                        "• Débranchez les appareils concernés\n" +
                        "• Un électricien interviendra en urgence si nécessaire");

        SOLUTIONS_CLIENT.put("Chauffage",
                "🔥 Problème de chauffage - Solutions :\n" +
                        "• Vérifiez le thermostat\n" +
                        "• Purgez les radiateurs\n" +
                        "• Vérifiez la pression de la chaudière\n" +
                        "• Un chauffagiste vous contactera");

        SOLUTIONS_CLIENT.put("Général",
                "🔍 Diagnostic général :\n" +
                        "• Redémarrez l'appareil\n" +
                        "• Vérifiez les branchements\n" +
                        "• Notez les messages d'erreur\n" +
                        "• Un technicien vous aidera à résoudre le problème");

        // Niveau d'urgence
        URGENCE.put("inondation", "🔴 URGENCE - Intervention immédiate");
        URGENCE.put("incendie", "🔴 URGENCE - Appelez les pompiers");
        URGENCE.put("gaz", "🔴 URGENCE - Évacuez et appelez");
        URGENCE.put("court-circuit", "🟠 Élevée");
        URGENCE.put("panne", "🟡 Moyenne");
        URGENCE.put("bruit", "🟢 Faible");

        // Type de technicien suggéré
        TECHNICIEN_SUGGERE.put("Réseau", "👨‍💻 Technicien réseau");
        TECHNICIEN_SUGGERE.put("Performance", "👨‍💻 Technicien informatique");
        TECHNICIEN_SUGGERE.put("Plomberie", "👨‍🔧 Plombier");
        TECHNICIEN_SUGGERE.put("Électricité", "👨‍🔧 Électricien");
        TECHNICIEN_SUGGERE.put("Chauffage", "👨‍🔧 Chauffagiste");
    }

    public static class DiagnosticClientResult {
        private String typeProbleme;
        private String solutionClient;
        private String urgence;
        private String technicienSuggere;
        private List<String> conseils;
        private List<String> motsCles;
        private int confiance;

        public DiagnosticClientResult() {
            this.conseils = new ArrayList<>();
            this.motsCles = new ArrayList<>();
        }

        public String getTypeProbleme() { return typeProbleme; }
        public void setTypeProbleme(String typeProbleme) { this.typeProbleme = typeProbleme; }

        public String getSolutionClient() { return solutionClient; }
        public void setSolutionClient(String solutionClient) { this.solutionClient = solutionClient; }

        public String getUrgence() { return urgence; }
        public void setUrgence(String urgence) { this.urgence = urgence; }

        public String getTechnicienSuggere() { return technicienSuggere; }
        public void setTechnicienSuggere(String technicienSuggere) { this.technicienSuggere = technicienSuggere; }

        public List<String> getConseils() { return conseils; }
        public void addConseil(String conseil) { this.conseils.add(conseil); }

        public List<String> getMotsCles() { return motsCles; }
        public void addMotCle(String mot) { this.motsCles.add(mot); }

        public int getConfiance() { return confiance; }
        public void setConfiance(int confiance) { this.confiance = confiance; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🔍 DIAGNOSTIC POUR CLIENT\n");
            sb.append("══════════════════════════\n");
            sb.append("📌 Problème détecté: ").append(typeProbleme).append("\n");
            sb.append("📊 Confiance: ").append(confiance).append("%\n");
            sb.append("⚠️ Urgence: ").append(urgence != null ? urgence : "Non déterminée").append("\n");
            sb.append("👤 Technicien recommandé: ").append(technicienSuggere != null ? technicienSuggere : "À déterminer").append("\n\n");
            sb.append("💡 QUE FAIRE EN ATTENDANT:\n");
            sb.append(solutionClient).append("\n\n");
            if (!conseils.isEmpty()) {
                sb.append("📝 CONSEILS SUPPLÉMENTAIRES:\n");
                for (String conseil : conseils) {
                    sb.append("  • ").append(conseil).append("\n");
                }
            }
            return sb.toString();
        }
    }

    public DiagnosticClientResult diagnostiquerPourClient(String description) {
        DiagnosticClientResult result = new DiagnosticClientResult();

        if (description == null || description.trim().isEmpty()) {
            result.setTypeProbleme("Non spécifié");
            result.setSolutionClient(SOLUTIONS_CLIENT.get("Général"));
            result.setConfiance(0);
            return result;
        }

        String texte = description.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();

        // Analyser les mots-clés
        for (Map.Entry<String, String> entry : KEYWORDS_TO_TYPE.entrySet()) {
            if (texte.contains(entry.getKey())) {
                String type = entry.getValue();
                scores.put(type, scores.getOrDefault(type, 0) + 1);
                result.addMotCle(entry.getKey());
            }
        }

        // Détection d'urgence
        for (Map.Entry<String, String> entry : URGENCE.entrySet()) {
            if (texte.contains(entry.getKey())) {
                result.setUrgence(entry.getValue());
                break;
            }
        }

        // Trouver le type majoritaire
        String typeTrouve = "Général";
        int maxScore = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                typeTrouve = entry.getKey();
            }
        }

        result.setTypeProbleme(typeTrouve);
        result.setSolutionClient(SOLUTIONS_CLIENT.getOrDefault(typeTrouve, SOLUTIONS_CLIENT.get("Général")));
        result.setTechnicienSuggere(TECHNICIEN_SUGGERE.getOrDefault(typeTrouve, "Technicien polyvalent"));
        result.setConfiance(Math.min(100, maxScore * 20));

        // Conseils par défaut
        result.getConseils().add("Prenez des photos du problème si possible");
        result.getConseils().add("Notez quand le problème a commencé");
        result.getConseils().add("Le technicien vous contactera sous 24h");

        return result;
    }
}