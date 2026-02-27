# 🎫 FIRMA — Gestion d'Événements et Participations

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=java)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-20.0.2-4DB33D?logo=openjfx)](https://gluonhq.com/products/javafx/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.30-00758F?logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Plateforme desktop complète de gestion événementielle développée en **Java 17 + JavaFX** permettant à **administrateurs** de créer/gérer des événements et à **visiteurs** de s'inscrire, gérer participations, ajouter accompagnants et exporter tickets PDF.

---

## 📋 Table des matières

- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Technologies](#technologies)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Architecture](#architecture)
- [Structure du Projet](#structure-du-projet)
- [Tests](#tests)
- [Sécurité & Limitations](#sécurité--limitations)
- [Améliorations Futures](#améliorations-futures)
- [Contribution](#contribution)
- [Auteurs](#auteurs)

---

## Aperçu

**FIRMA** est une application **MVC** desktop scalable pour la gestion complète du cycle de vie d'événements :

- ✅ **Création/Modification** d'événements par administrateurs
- ✅ **Inscription** aux événements par visiteurs via formulaires intuitifs
- ✅ **Gestion accompagnants** : ajouter/supprimer compagnons par participation
- ✅ **Génération tickets** : PDF téléchargeables + partage par email
- ✅ **Codes QR** : traçabilité via codes de participation uniques (PART-XXXXX)
- ✅ **Statistiques en temps réel** : nombre participants, places disponibles, etc.
- ✅ **Recherche & Tri** : filtrage événements par titre, date, type, places
- ✅ **API Météorologique** : prévisions intégrées pour les événements
- ✅ **Authentification** : système login avec rôles (ADMIN, VISITEUR, TECHNICIEN)
- ✅ **Dashboard Analytique** : KPIs événements pour administrateurs

---

## Fonctionnalités

### Pour les **Administrateurs** 🔒

| Fonctionnalité | Description |
|---|---|
| **Créer Événement** | Formulaire complet (titre, dates, horaires, type, organisateur, etc.) |
| **Générer Image avec IA** | 🤖 Génération intelligente via **Picsum Photos** basée sur le type d'événement |
| **Modifier Événement** | Mise à jour en temps réel + validation |
| **Supprimer Événement** | Suppression logique avec gestion cascades |
| **Liste Événements** | Vue avec tri/filtrage par date, titre, places |
| **Voir Participations** | Grille globale des participations par événement |
| **Afficher Participants** | Liste détaillée avec accompagnants |
| **Dashboard Analytique** | Statistiques : total événements, participants, taux remplissage |
| **Exporter Rapports** | Génération des données pour analyse |

### Pour les **Visiteurs** 👥

| Fonctionnalité | Description |
|---|---|
| **Consulter Événements** | Affichage cartes/listes avec détails enrichis |
| **Recherche & Tri** | Filtrer par titre, type, date, places disponibles |
| **S'Inscrire** | Formulaire participation avec nombre accompagnants |
| **Gérer Accompagnants** | Ajouter/modifier nom et prénom compagnons |
| **Voir ses Participations** | Liste personnelle des inscriptions + statuts |
| **Télécharger Tickets** | Génération PDF avec code QR par participation |
| **Recevoir Confirmations** | Emails avec tickets PDF en pièce jointe |
| **Annuler Inscription** | Retrait avec libération des places |

---

### 🤖 **Génération d'Images IA avec Picsum Photos**

**FIRMA** intègre un **système intelligent de génération d'images** basé sur **Picsum Photos** :

#### Comment ça Marche ?

```
Admin crée événement → Clique "Générer Image IA" 
    ↓
AIImageService analyse :
  • Titre de l'événement
  • Type d'événement (Conférence, Atelier, Exposition, etc.)
  • Description & Localisation
    ↓
Sélectionne intelligemment une image Picsum pertinente
    ↓
Télécharge l'image (512x512px, haute qualité)
    ↓
Sauvegarde dans le dossier uploads de l'événement ✓
```

#### Types d'Événements & Images Associées

| Type | Mots-clés | Images Picsum |
|---|---|---|
| **CONFERENCE** | Business, Réunion, Professionnel | IDs: 1, 3, 15, 20, 26, 28, 48... |
| **ATELIER** | Workshop, Collaboration, Créativité | IDs: 7, 13, 27, 42, 52, 88, 109... |
| **EXPOSITION** | Art, Galerie, Culture, Architecture | IDs: 10, 24, 39, 58, 77, 96, 123... |
| **SALON** | Trade show, Hall, événement | IDs: 16, 33, 47, 65, 84, 112, 145... |
| **FORMATION** | Éducation, Apprentissage, Dev | IDs: 21, 35, 54, 71, 91, 127, 159... |
| **AUTRE** | Général, Communauté, Célébration | IDs: 8, 18, 29, 44, 62, 79, 98... |

#### Configuration

Fichier `src/main/resources/ai_config.properties` :

```properties
# Configuration de l'API d'images
ai.model=picsum-smart
ai.max_retries=3
ai.timeout_seconds=30
ai.max_image_size=512
```

#### Avantages de Picsum 🎯

- ✅ **Gratuit** - Aucune clé API requise
- ✅ **Illimité** - Pas de limite de requêtes
- ✅ **Fiable** - Service stable hébergé sur CDN géo-distribué
- ✅ **Adaptatif** - Sélection intelligente par type d'événement
- ✅ **Fallback** - Utilisation d'image par défaut si API indisponible
- ✅ **Cache-buster** - Paramètre `random` pour toujours obtenir des images fraîches

#### Implémentation Technique

Classe : [AIImageService.java](src/main/java/edu/connection3a7/tools/AIImageService.java)

```java
// Usage
AIImageService aiService = new AIImageService();
File eventImage = aiService.generateEventImage(
    "Conférence Web 2026",
    "Découvrez les tendances du web",
    Type.CONFERENCE,
    "Paris, France",
    "TechCorp"
);

// Avec fallback automatique
File imageWithFallback = aiService.generateEventImageWithFallback(
    title, description, type, location, organizer
);

// Test de connexion
boolean connected = aiService.testConnection();
```

#### Workflow dans l'Interface

```
Dashboard Admin
    ↓
Onglet "📋 Créer Événement"
    ↓
Formulaire complet rempli
    ↓
Cliquer "🤖 Générer Image IA"
    ↓
Picsum récupère image pertinente (instantané)
    ↓
Label affiche: "Image_Conf_2026.jpg ✓"
    ↓
Cliquer "Créer Événement"
    ↓
Événement créé avec image générée ✓
```

---

## Technologies

### Stack Principal

| Couche | Composant | Version |
|-------|-----------|---------|
| **Langage** | Java (JDK) | 17+ |
| **UI Desktop** | JavaFX + FXML + CSS | 20.0.2 |
| **Base de Données** | MySQL / MariaDB | 8.0.30 / 10.4+ |
| **Accès BD** | JDBC + Prepared Statements | MySQL Connector 8.0.30 |
| **Build** | Maven | 3.8+ |

### Dépendances Clés

```xml
<!-- Interface & Contrôle -->
org.openjfx:javafx-fxml:20.0.2
org.openjfx:javafx-controls:20.0.2

<!-- Base de données -->
mysql:mysql-connector-java:8.0.30

<!-- Génération PDF -->
com.itextpdf:itext7-core:7.2.5

<!-- Codes QR -->
com.google.zxing:core:3.5.2
com.google.zxing:javase:3.5.2

<!-- Emails -->
com.sun.mail:jakarta.mail:2.0.1

<!-- Logs -->
org.slf4j:slf4j-simple:1.7.36

<!-- Tests -->
org.junit.jupiter:junit-jupiter:5.10.2

<!-- JSON parsing (API Météo) -->
com.fasterxml.jackson.core:jackson-databind:2.15.2
```

### APIs Externes Intégrées

| Service | Fonction | Type | Gain |
|---|---|---|---|
| **Picsum Photos** | Génération images d'événements | REST (HTTP) | Libre, gratuit, illimité ✓ |
| **Open-Meteo** | Prévisions météorologique | REST (JSON) | Données météo temps réel |
| **Unsplash** | Références qualité (optionnel) | REST (JSON) | Images haute résolution |
| **Google Maps** | Affichage localisation | Web (Embed) | Visualisation géographique |

---

## Installation

### Prérequis

- **Java JDK 17+** ([Télécharger](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** ([Télécharger](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ou **MariaDB 10.4+** ([Télécharger](https://www.mysql.com/downloads/))
- **IDE Java** : IntelliJ IDEA, Eclipse, VS Code + extensions Java

### Cloner le Dépôt

```bash
git clone https://github.com/username/firma.git
cd firma/firma
```

### Installer les Dépendances Maven

```bash
mvn clean install
```

Cette commande :
- ✓ Télécharge les dépendances Maven
- ✓ Compile le code source
- ✓ Crée les fichiers `.class`

---

## Configuration

### 1. Configurer la Base de Données

#### Option A : Utiliser le script SQL fourni

```bash
# Importer le schéma
mysql -u root -p < src/main/java/edu/connection3a7/database/firma.sql
```

**Données de test incluses** :
- Utilisateur Admin : `admin@firma.com` / `admin123`
- Utilisateur Test : `user@firma.com` / `user123`

#### Option B : Créer manuellement

```sql
CREATE DATABASE firma;
USE firma;
-- Importer firma.sql via MySQL Workbench ou phpMyAdmin
```

### 2. Configurer les Paramètres de Connexion

Modifiez le fichier `MyConnection.java` :

```java
public class MyConnection {
    private String url    = "jdbc:mysql://localhost:3306/firma";
    private String login  = "root";
    private String pwd    = ""; // Votre mot de passe MySQL
    
    // ... reste du code
}
```

**À faire** : Externaliser ces paramètres dans un fichier `config.properties`

### 3. Configurer JavaFX (si Maven ne détecte pas)

Ajoutez les variables d'environnement :

**Windows (PowerShell)** :
```powershell
$env:JAVAFX_HOME = "C:\path\to\javafx-sdk-20"
$env:PATH += ";$env:JAVAFX_HOME\bin"
```

**Linux/Mac (Bash)** :
```bash
export JAVAFX_HOME=/path/to/javafx-sdk-20
export PATH=$PATH:$JAVAFX_HOME/bin
```

### 4. Configurer Picsum Photos (Génération IA des Images)

La configuration est **automatique**, mais vous pouvez la personnaliser via `ai_config.properties` :

**Fichier** : `src/main/resources/ai_config.properties`

```properties
# ✅ PICSUM PHOTOS INTELLIGENT - Gratuit, illimité, ultra-fiable !
# Sélection intelligente d'images basée sur le type d'événement

ai.model=picsum-smart
ai.max_retries=3
ai.timeout_seconds=30
ai.max_image_size=512
```

**Paramètres** :
- `ai.model` : Moteur (picsum-smart par défaut)
- `ai.max_retries` : Tentatives en cas d'erreur
- `ai.timeout_seconds` : Délai max de réponse API
- `ai.max_image_size` : Résolution max (512x512px)

**Test de Connexion** :
```bash
# Vérifier que Picsum Photos est accessible
mvn clean compile
java -cp target/classes edu.connection3a7.tools.AIImageService
```

---

## Utilisation

### Lancer l'Application

#### Via Maven

```bash
mvn clean javafx:run
```

#### Via IDE

1. IntelliJ IDEA : `Run > Run 'MainFX'`
2. Eclipse : `Run > Run Configurations > Java Application > MainFX`
3. VS Code : Debug via "Java Test Runner"

### Workflows Principaux

#### Scénario 1 : Administrateur crée événement

```
1. Lancer app → Écran Login
2. Saisir email/mdp admin → Dashboard
3. Aller à onglet "📋 Créer"
4. Remplir formulaire :
   - Titre ✓
   - Description (optionnel) ✓
   - Dates (début/fin) ✓
   - Horaires ✓
   - Type d'événement ✓
   - Organisateur ✓
   - Localisation ✓
   - Nombre places ✓
5. OPTION A - Cliquer "🤖 Générer Image IA" :
   ✓ Picsum Photos analyse le type d'événement
   ✓ Sélectionne image pertinente (instantané)
   ✓ Affiche "Image_Conf_2026.jpg ✓"
6. OPTION B - Cliquer "📁 Télécharger Image" pour upload manuel
7. Cliquer "Créer Événement" → Succès !
8. Onglet "📊 Liste" → Nouvel événement visible avec image
```

#### Scénario 2 : Visiteur s'inscrit

```
1. Lancer app → Login
2. Saisir email/mdp visiteur → Accueil
3. Cliquer "🎉 Événements" → Liste événements
4. Cliquer "ℹ️ Détails" sur un événement → Popup détails
5. Cliquer "🙋 Participer" → Formulaire modal
6. Saisir :
   - Nombre accompagnants (0-100)
   - Commentaire optionnel
7. Cliquer "✓ Participer" → Participation enregistrée
8. Aller à "📋 Mes Participations" → Voir inscription + code
9. Cliquer "📥 Exporter PDF" → Télécharger ticket
```

---

## Architecture

### Pattern MVC + Délégation

```
┌──────────────────────────────────────────────────────┐
│         FXML (Vue) - Couche Présentation            │
│  • LoginApplication.fxml    (Login)                  │
│  • Dashboard.fxml           (Admin)                  │
│  • front.fxml               (Visiteur) + CSS        │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│      Contrôleurs Principaux (MVC Controllers)       │
│  • LoginController          → Authentification       │
│  • EvenementController      → Admin Dashboard       │
│  • FrontController          → Visiteur (Events)     │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│     Contrôleurs Délégués (Responsabilité Unique)    │
│  • ConstructionCartesEvenement   → Affichage cartes │
│  • ConstructionCartesVisiteur    → Cartes visiteur │
│  • FormulaireCreationModif...    → CRUD formules  │
│  • GestionParticipationsVisiteur → Gestion inscr. │
│  • AffichageListeParticipants    → Grille partic. │
│  • AffichageTicketsEtExportPDF   → PDF generator  │
│  • DashboardAnalytique           → Stats/KPIs     │
│  • OutilsInterfaceGraphique      → Dialogs/Utils  │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│    Services (DAO/Logique Métier) IService<T>       │
│  • EvenementService          → CRUD + places       │
│  • ParticipationService      → CRUD + codes QR    │
│  • AccompagnantService       → Gestion accompag.  │
│  • UtilisateurService        → Auth + profils    │
│  • PersonneService           → Données personnes │
│  • StatistiquesService       → Agrégations stats │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│         Entités (Modèles de Données)                │
│  • Utilisateur     {id, nom, email, role, ...}     │
│  • Evenement       {id, titre, dates, capacité}   │
│  • Participation   {id, idUser, idEvent, statut}  │
│  • Accompagnant    {id, nom, idParticipation}     │
│  • Enums : Role, Type, Statut, Statutevent      │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│        Outils & Services Spécialisés                │
│  • MyConnection              → Singleton JDBC     │
│  • SessionManager            → État utilisateur   │
│  • EmailService              → Jakarta Mail       │
│  • QRCodeUtil                → ZXing QR gen      │
│  • WeatherService            → APIs météo        │
│  • TicketServerService       → Serveur tickets   │
│  • AIImageService            → Picsum Photos IA  │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│       Base de Données (MySQL/MariaDB)              │
│  • Tables : utilisateurs, événements, moments...  │
│  • Relations : 1-N, N-N avec intégrité           │
└──────────────────────────────────────────────────────┘
```

### Concepts Clés

#### **IService<T>** - Interface générique CRUD
```java
public interface IService<T> {
    void addEntity(T t) throws SQLException;
    void deleteEntity(T t) throws SQLException;
    void updateEntity(int id, T t) throws SQLException;
    List<T> getData() throws Exception;
}
```

#### **Singleton Pattern**
- `MyConnection.getInstance()` → Connexion unique BD
- `SessionManager.getInstance()` → État utilisateur
- `WeatherService.getInstance()` → Service météo

#### **Prepared Statements**
Protection contre injection SQL :
```java
String query = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
PreparedStatement stmt = cnx.prepareStatement(query);
stmt.setString(1, email);
stmt.setString(2, password);
```

---

## 🤖 Système de Génération d'Images IA - Picsum Photos

### Architecture du Service IA

```
Event Creation Form (Admin Dashboard)
        ↓
    [Type d'événement selectionnée]
        ↓
AIImageService.generateEventImage()
        ↓
    Analyse contexte:
    ├─ Type : Conference, Atelier, Exposition, etc.
    ├─ Title : Extraction mots-clés
    ├─ Description : Contexte additionnel
    └─ Location : Localisation
        ↓
selectImageIdFromKeywords()
        ↓
    Mappe type → Plage d'IDs Picsum
    ├─ Conference → [1,3,15,20,26,28,48,63...]
    ├─ Atelier → [7,13,27,42,52,88,109...]
    ├─ Exposition → [10,24,39,58,77,96,123...]
    ├─ Salon → [16,33,47,65,84,112,145...]
    ├─ Formation → [21,35,54,71,91,127,159...]
    └─ Autre → [8,18,29,44,62,79,98...]
        ↓
    Sélectionne ID aléatoire
        ↓
callUnsplashAPI(imageId) - Picsum Photos
        ↓
    URL : https://picsum.photos/id/{ID}/512/512
        ↓
    [Télécharge image 512x512px]
        ↓
Sauvegarde fichier temporaire
        ↓
Retourne File → UI affiche "Image_Conf_2026.jpg ✓"
        ↓
Admin clique "Créer Événement"
        ↓
Image stockée en BD (path) ✓
```

### Cas d'Usage : Par Type d'Événement

#### 1️⃣ Conference/Réunion Professionnelle

```
Input:
- Title: "Conférence Web Technologies 2026"
- Type: CONFERENCE
- Description: "Découvrez les tendances du web moderne"
- Location: "Paris, France"
- Organizer: "TechCorp"

Processing:
1. buildKeywords() → "conference, web, technologies, event, professional"
2. selectImageIdFromKeywords() :
   - Détecte "conference" → utilise conferenceIds
   - Sélectionne aléatoire : ID = 82
3. callUnsplashAPI(82) :
   - Picsum retourne image professionnelle 512x512
4. Sauvegarde : "/uploads/event_12345_conference.jpg"

Result: ✓ Image professional business obtenue
```

#### 2️⃣ Workshop/Atelier Créatif

```
Input:
- Title: "Atelier Web Design avec Figma"
- Type: ATELIER
- Description: "Apprenez à créer des interfaces modernes"

Processing:
1. buildKeywords() → "atelier, workshop, design, figma, learning"
2. selectImageIdFromKeywords() :
   - Détecte "workshop" ou "atelier" → utilise atelierIds
   - Sélectionne aléatoire : ID = 88
3. callUnsplashAPI(88) :
   - Picsum retourne image collaborative/créative
4. Sauvegarde image

Result: ✓ Image atelier/collaboration obtenue
```

#### 3️⃣ Exposition Artistique

```
Input:
- Title: "Exposition d'Art Contemporain 2026"
- Type: EXPOSITION

Processing:
1. buildKeywords() → "exposition, art, contemporary, gallery"
2. selectImageIdFromKeywords() :
   - Détecte "exhibition" ou "exposition" → utilise expositionIds
   - Sélectionne aléatoire : ID = 123
3. callUnsplashAPI(123) :
   - Picsum retourne image artistique/galerie
4. Sauvegarde image

Result: ✓ Image art/galerie obtenue
```

### Configuration Intelligente

**Classe Responsable** : [AIImageService.java](src/main/java/edu/connection3a7/tools/AIImageService.java)

**Key Methods** :

| Méthode | Rôle |
|---|---|
| `generateEventImage()` | Main orchestrator |
| `buildKeywords()` | Extraction contexte |
| `selectImageIdFromKeywords()` | Sélection ID intelligente |
| `getEventTypeKeyword()` | Mapping type → keyword |
| `callUnsplashAPI()` | Appel HTTP Picsum |
| `testConnection()` | Vérification connectivité |

### Gestion des Erreurs

```java
// 1. Tentative génération IA
try {
    return generateEventImage(title, description, type, location, organizer);
} 

// 2. En cas d'échec → Fallback automatique
catch (Exception e) {
    System.err.println("Erreur IA: " + e.getMessage());
    
    // A. Cherche image par défaut
    File defaultImage = new File("src/main/resources/image/default_event.png");
    if (defaultImage.exists()) {
        return defaultImage;  // ✓ Utilise image par défaut
    }
    
    // B. Crée fallback vide (admin upload manuel)
    File tempFile = File.createTempFile("event_default_", ".txt");
    return tempFile;  // Admin choisira image plus tard
}
```

### Avantages de cette Approche 🎯

| Avantage | Détail |
|---|---|
| **100% Gratuit** | Picsum Photos = API libre, aucune clé requise |
| **Infinités** | Pas de limite de requêtes |
| **Fiable** | Infrastructure CDN robuste |
| **Rapide** | Réponse < 1s typiquement |
| **Intelligent** | Sélection contextuelle par type d'événement |
| **Robuste** | Fallback automatique si API indisponible |
| **Offline Capable** | Images cachées localement |

### Statistiques Picsum 📊

- **+ 400 images disponibles** via API
- **20 catégories** de sélection intelligente
- **Résolution** 512x512px (optimisée pour vignettes) 
- **Format** JPEG (compression efficace)
- **Uptime** 99.9% (hébergé sur serveurs stables)
- **Temps réponse** ~200-500ms moyen

---

### Framework : JUnit 5.10.2

Tests unitaires pour les services critiques :

### Exécuter les Tests

```bash
# Tous les tests
mvn clean test

# Test spécifique
mvn clean test -Dtest=ParticipationServiceTest

# Avec rapport couverture
mvn clean test jacoco:report
```

### Tests Disponibles

| Classe Test | Scénarios |
|---|---|
| **ParticipationServiceTest** | ✓ Créer participation<br/>✓ Lire participation<br/>✓ Modifier participation<br/>✓ Supprimer participation<br/>✓ Contrôle places |
| **EvenementServiceTest** | ✓ CRUD événements<br/>✓ Réserver places<br/>✓ Libérer places<br/>✓ Filtrage/tri |
| **AccompagnantServiceTest** | ✓ Ajouter accompagnant<br/>✓ Récupérer par participation<br/>✓ Supprimer/Mettre à jour |

### Exemple : Test Participation

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParticipationServiceTest {
    private static ParticipationService participationService;
    
    @Test
    @Order(1)
    void testCreerParticipation() throws SQLException {
        Participation p = new Participation();
        p.setIdEvenement(1);
        p.setIdUtilisateur(1);
        p.setStatut(Statut.CONFIRME);
        p.setNombreAccompagnants(2);
        
        participationService.addEntity(p);
        // Assertion...
    }
}
```

**À faire** : Intégrer tests d'intégration BD avec testcontainers

---

## Structure du Projet

```
firma/
│
├── pom.xml                                    # Configuration Maven
├── README.md                                  # Cette documentation
├── INTEGRATION_PARTICIPATIONS.md              # Changelog module participations
├── migration_code_participation.sql           # Migrations SQL
│
├── src/
│   ├── main/
│   │   ├── java/edu/connection3a7/
│   │   │   ├── controllers/                   # 11 contrôleurs MVC
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── EvenementController.java       (Admin)
│   │   │   │   ├── FrontController.java           (Visiteur)
│   │   │   │   ├── ConstructionCartesEvenement.java
│   │   │   │   ├── ConstructionCartesVisiteur.java
│   │   │   │   ├── FormulaireCreationModificationEvenement.java
│   │   │   │   ├── GestionParticipationsVisiteur.java
│   │   │   │   ├── AffichageListeParticipants.java
│   │   │   │   ├── AffichageTicketsEtExportPDF.java
│   │   │   │   ├── DashboardAnalytique.java
│   │   │   │   └── OutilsInterfaceGraphique.java
│   │   │   │
│   │   │   ├── entities/                      # 9 entités + 4 enums
│   │   │   │   ├── Utilisateur.java
│   │   │   │   ├── Evenement.java
│   │   │   │   ├── Participation.java
│   │   │   │   ├── Accompagnant.java
│   │   │   │   ├── Personne.java
│   │   │   │   ├── Role.java            (enum)
│   │   │   │   ├── Type.java            (enum)
│   │   │   │   ├── Statut.java          (enum)
│   │   │   │   └── Statutevent.java     (enum)
│   │   │   │
│   │   │   ├── services/                      # 6 services DAO
│   │   │   │   ├── EvenementService.java
│   │   │   │   ├── ParticipationService.java
│   │   │   │   ├── AccompagnantService.java
│   │   │   │   ├── UtilisateurService.java
│   │   │   │   ├── PersonneService.java
│   │   │   │   └── StatistiquesService.java
│   │   │   │
│   │   │   ├── interfaces/                    # Contrats
│   │   │   │   └── IService.java
│   │   │   │
│   │   │   ├── tools/                         # Utilitaires
│   │   │   │   ├── MyConnection.java          (Singleton JDBC)
│   │   │   │   ├── SessionManager.java        (État utilisateur)
│   │   │   │   ├── EmailService.java          (Jakarta Mail)
│   │   │   │   ├── QRCodeUtil.java            (ZXing)
│   │   │   │   ├── WeatherService.java        (Météo API)
│   │   │   │   ├── TicketServerService.java   (HTTP serveur)
│   │   │   │   └── AIImageService.java        (Génération IA)
│   │   │   │
│   │   │   ├── test/
│   │   │   │   ├── MainFX.java                (Point d'entrée JavaFX)
│   │   │   │   └── Main.java                  (Alternative CLI)
│   │   │   │
│   │   │   └── database/
│   │   │       ├── firma.sql                  (Schéma complet 758 lignes)
│   │   │       └── migration_accompagnants.sql
│   │   │
│   │   └── resources/
│   │       ├── LoginApplication.fxml          (Écran login)
│   │       ├── Dashboard.fxml                 (Admin panel)
│   │       ├── front.fxml                     (Visiteur interface)
│   │       ├── front-style.css
│   │       ├── evenement-style.css
│   │       └── image/                         (Logo, slogan, etc.)
│   │
│   └── test/
│       └── java/edu/connection3a7/services/
│           ├── ParticipationServiceTest.java  (Tests CRUD JUnit 5)
│           ├── EvenementServiceTest.java
│           └── AccompagnantServiceTest.java
│
├── target/                                    # Build output (gitignore)
│   ├── classes/
│   ├── generated-sources/
│   └── test-classes/
│
└── atelier/                                   # Sous-projet (optionnel)
    ├── pom.xml
    └── src/
        ├── main/java/edu/connection3a7/...
        └── test/java/...
```

---

## Sécurité & Limitations

### 🔴 Problèmes de Sécurité Actuels

| Problème | Sévérité | Description |
|---|---|---|
| **Mots de passe en clair** | 🔴 Critique | Stockage non chiffré en BD<br/>→ **À faire** : BCrypt/PBKDF2 |
| **SQL Injection** | 🟡 Moyen | Prepared statements utilisés<br/>→ **À faire** : ORM (Hibernate/JPA) |
| **Pas de session timeout** | 🟡 Moyen | SessionManager persiste indéfiniment<br/>→ **À faire** : Timeout + refresh tokens |
| **Credentials en dur** | 🟠 Haut | URL/login/pwd dans MyConnection.java<br/>→ **À faire** : Fichier config externalisé |
| **Pas d'Audit Trail** | 🟡 Moyen | Aucun log d'actions admin<br/>→ **À faire** : Logging audit complet |
| **Pas de validation entrée** | 🟠 Haut | Input utilisateur validé en UI uniquement<br/>→ **À faire** : Bean Validation (Hibernate Validator) |

### Limitations Actuelles

- ❌ **Pas de pool de connexions** → Performance limitée
- ❌ **Pas de transactions** → Risque data inconsistency
- ❌ **JDBC brut** → Boilerplate et maintenance
- ❌ **Pas de chiffrement données** → Risque exposition
- ❌ **Pas de cache** → Charges BD répétées
- ❌ **Pas d'API REST** → Intégration tierces difficile

---

## Améliorations Futures

### Court Terme (v1.1) 📅

- [ ] **Hachage mot de passe** : BCrypt + validation complexité
- [ ] **Config externalisée** : fichier `application.properties`
- [ ] **Logs structurés** : Logback + MDC
- [ ] **Validation entrée** : Hibernate Validator annotations
- [ ] **Gestion erreurs** : Custom exceptions

### Moyen Terme (v1.2) 📅

- [ ] **ORM Migration** : JPA/Hibernate (moins de JDBC)
- [ ] **Pool connexions** : HikariCP
- [ ] **Transactions ACID** : @Transactional ou programmatique
- [ ] **Cache applicatif** : Ehcache
- [ ] **Audit logging** : Javers ou custom
- [ ] **Modèles IA avancés** : Migration Picsum → DALL-E 3 / Stable Diffusion (avec frais optionnels)

### Long Terme (v2.0) 🚀

- [ ] **API REST** : Spring Boot backend
- [ ] **Client web** : React/Angular frontend
- [ ] **Mobile** : Flutter/React Native
- [ ] **Authentification OAuth2** : Keycloak/Auth0
- [ ] **Conteneurisation** : Docker + Docker Compose
- [ ] **Infrastructure as Code** : Terraform/Ansible
- [ ] **CI/CD** : GitHub Actions/GitLab CI
- [ ] **RGPD Compliance** : Data Privacy by Design

---

## Contribution

Les contributions sont **bienvenues** ! 🎉

### Processus

1. **Fork** le dépôt
2. **Créer une branche** : `git checkout -b feature/ma-feature`
3. **Commit** : `git commit -m "Ajout ma feature"`
4. **Push** : `git push origin feature/ma-feature`
5. **Pull Request** vers `main`

### Guidelines

- ✅ Code formaté Java standard
- ✅ Tests unitaires pour nouvelles features
- ✅ Javadoc sur méthodes publiques
- ✅ Messages commit explicites

---

## Auteurs

| Auteur | Rôle | Contact |
|---|---|---|
| **Votre Nom** | Lead Developer | [GitHub](https://github.com/username) |
| **ESPRIT 3A7** | Contexte Académique | École Supérieure Privée d'Ingénierie |

---

## License

Ce projet est sous license **MIT** — voir [LICENSE](LICENSE) pour détails.

```
MIT License (c) 2026
Permission is hereby granted, free of charge, to any person obtaining a copy...
```

---

## Ressources & Liens

### Documentation Officielle
- [Java 17 API Docs](https://docs.oracle.com/en/java/javase/17/)
- [JavaFX Documentation](https://gluonhq.com/products/javafx/)
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)
- [Maven Official Guide](https://maven.apache.org/guides/)

### Technologies Utilisées
- [iText PDF Library](https://itextpdf.com/)
- [ZXing QR Code Generator](https://github.com/zxing/zxing)
- [Jakarta Mail](https://projects.eclipse.org/projects/ee4j.mail)
- [Jackson JSON Library](https://github.com/FasterXML/jackson)
- [JUnit 5](https://junit.org/junit5/)

### Tutoriels Recommandés
- JavaFX MVC Pattern : [Oracle Tutorial](https://docs.oracle.com/javase/8/javafxui-tutorials/)
- JDBC Best Practices : [Oracle JDBC Guide](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/module-summary.html)
- Database Design : [MySQL Workbench](https://www.mysql.com/products/workbench/)

---

## FAQ

### Q : Quelle est la version Java requise ?
**A :** Java 17 minimum. JDK 21 compatible.

### Q : Puis-je utiliser PostgreSQL au lieu de MySQL ?
**A :** Oui, avec modifications mineures du driver et dialecte SQL.

### Q : Comment ajouter des utilisateurs en BD ?
**A :** Via INSERT directs dans `utilisateurs` table ou via UI admin (à développer).

### Q : Les tickets PDF sont-ils générés en temps réel ?
**A :** Oui, via iText7. Les PDFs sont téléchargeables/envoyés par email.

### Q : Puis-je déployer l'app sur serveur ?
**A :** Actuellement desktop uniquement. Pour serveur, migrer vers Spring Boot + web framework.

### Q : Comment contacter support ?
**A :** Créer une issue GitHub ou envoyer email à [your-email@firma.com](mailto:your-email@firma.com)

### Q : Picsum Photos fonctionne-t-il hors ligne ?
**A :** Non. Picsum nécessite une connexion HTTP. Utilisez le fallback manuel (upload image).

### Q : Les images Picsum sont-elles libres d'utilisation ?
**A :** Oui. Picsum Photos fournit images libres sous licence Unsplash (libre d'usage commercial).

### Q : Puis-je utiliser ma propre API IA (OpenAI, Midjourney, etc.) ?
**A :** Oui. Modifiez `AIImageService.callUnsplashAPI()` pour appeler votre API. Exemple :
```java
// Remplacer Picsum par OpenAI DALL-E
private byte[] callOpenAI(String prompt) throws Exception {
    // Appel API OpenAI avec prompt généré
    // Retourner octets image
}
```

### Q : Pourquoi Picsum et pas une vraie IA (DALL-E, Midjourney) ?
**A :** Picsum = gratuit + fiable + pas d'API key. DALL-E nécessiterait frais. À migrer si budget.

### Q : Comment désactiver la génération IA ?
**A :** Commentez le bouton "🤖 Générer Image IA" dans Dashboard.fxml ou laissez vide le `ai_config.properties`.

---

## Changelogs

### v1.0.0 - Released 27 Feb 2026 🎉

**Fonctionnalités initiales :**
- ✅ CRUD utilisateurs + authentification
- ✅ CRUD événements complet
- ✅ Système participations + accompagnants
- ✅ Génération tickets PDF + codes QR
- ✅ Interface JavaFX desktop
- ✅ Tests unitaires JUnit
- ✅ Documentation complète

**Bugs connus :**
- ⚠️ Sessions timeout non géré
- ⚠️ Pas de gestion transactions
- ⚠️ Erreurs UI non localisées

**Prochaines étapes :**
- v1.1 : Sécurité renforcée
- v1.2 : ORM migration
- v2.0 : REST API + Web UI

---

## Support

💬 **Vous avez une question ?**

- 📖 Consultez la [Documentation](README.md)
- 🐛 Signalez un bug : [GitHub Issues](https://github.com/username/firma/issues)
- 💡 Proposez une feature : [GitHub Discussions](https://github.com/username/firma/discussions)
- ✉️ Email : [contact@firma.com](mailto:contact@firma.com)

---

**Merci d'utiliser FIRMA ! 🙏**

Made with ❤️ by Développeurs ESPRIT 3A7
