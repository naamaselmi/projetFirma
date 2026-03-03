# � FIRMA — Plateforme de Gestion Agricole

Application de bureau JavaFX complète pour la gestion d'une entreprise agricole : **Marketplace** (équipements, véhicules, terrains) et **Gestion d'Événements** (conférences, salons, ateliers…). Deux modules intégrés dans une interface unifiée avec authentification par rôle (admin / client).

---

## 📋 Table des matières

- [Aperçu](#aperçu)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [🔐 Sécurité & Configuration](#-sécurité--configuration) ⭐ **LIRE AVANT DE DÉMARRER**
- [Installation et Configuration](#installation-et-configuration)
- [Base de données](#base-de-données)
- [Lancement](#lancement)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Captures d'écran](#captures-décran)
- [Auteurs](#auteurs)

---

## Aperçu

**FIRMA** est une application Java/JavaFX développée dans le cadre d'un projet académique (ESPRIT 3A7). Elle intègre deux grands modules :

1. **Gestion Marketplace** — Catalogue de produits agricoles (équipements, véhicules, terrains), commandes, paiements Stripe, locations, gestion des fournisseurs, alertes de stock, et génération de reçus PDF.
2. **Gestion d'Événements** — Création et gestion d'événements, inscriptions avec accompagnants, tickets PDF avec QR codes, confirmation par email, météo intégrée, et génération d'images IA.

Chaque module possède un **tableau de bord** (admin & client) avec statistiques, graphiques, et raccourcis de navigation.

---

## Technologies

| Composant            | Technologie                          |
|----------------------|--------------------------------------|
| Langage              | Java 17                              |
| Interface            | JavaFX 20.0.2 + FXML + CSS          |
| Base de données      | MySQL 8.x / MariaDB 10.4            |
| Accès DB             | JDBC (MySQL Connector 8.0.30)        |
| Build                | Maven 3.8+                           |
| PDF                  | iText 7.2.5                          |
| Paiement             | Stripe Java SDK 24.0.0               |
| Email                | Jakarta Mail 2.0.1 (SMTP Gmail)      |
| QR Codes             | ZXing 3.5.2                          |
| JSON                 | Gson 2.10.1                          |
| Cartographie         | Leaflet (WebView) + Nominatim        |
| Météo                | Open-Meteo API                       |
| IA (images)          | Hugging Face Inference API (SDXL)    |
| Tests                | JUnit 5.10.2, jqwik 1.8.2            |
| Web (QR tickets)     | Serveur HTTP embarqué (JDK HttpServer)|

---

## Architecture

Le projet suit le pattern **MVC** (Modèle-Vue-Contrôleur) organisé en deux modules :

```
┌────────────────────────────────────────────────────────────────────┐
│                        VUES (FXML + CSS)                          │
│                                                                    │
│  LoginApplication.fxml                                             │
│  ├─ Admin : AdminDashboard.fxml → AdminAccueilContent.fxml         │
│  │          EquipementView, VehiculeView, TerrainView,             │
│  │          FournisseurView, CommandeAdminView, LocationAdminView   │
│  └─ Client: client_dashboard.fxml → ClientAccueilView.fxml        │
│             ClientMarketplaceView, ProductDetailView, CartPanelView│
│             PaymentView, RentalsPanelView                          │
│  ├─ Events: Dashboard.fxml, DashboardContent.fxml                  │
│  │          front.fxml, FrontContent.fxml, Accueil.fxml            │
│  └─ Styles: evenement-style.css, front-style.css, styles.css       │
├────────────────────────────────────────────────────────────────────┤
│                      CONTRÔLEURS                                   │
│                                                                    │
│  GestionMarketplace/                                               │
│    AdminDashboardController, AdminAccueilController                 │
│    ClientDashboardController, ClientAccueilController               │
│    ClientMarketplaceController, ProductDetailController             │
│    CartPanelController, PaymentController, RentalsPanelController   │
│    EquipementController, VehiculeController, TerrainController      │
│    FournisseurController, CommandeAdminController                   │
│    LocationAdminController, MarketplaceController                   │
│                                                                    │
│  GestionEvenement/                                                 │
│    LoginController, EvenementController, FrontController            │
│    AccueilController, DashboardAnalytique                           │
│    FormulaireCreationModificationEvenement                          │
│    ConstructionCartesEvenement, ConstructionCartesVisiteur           │
│    GestionParticipationsVisiteur, AffichageListeParticipants        │
│    AffichageTicketsEtExportPDF, OutilsInterfaceGraphique            │
├────────────────────────────────────────────────────────────────────┤
│                      SERVICES (DAO)                                │
│                                                                    │
│  GestionMarketplace/                                               │
│    EquipementService, VehiculeService, TerrainService               │
│    CategorieService, FournisseurService, AchatFournisseurService    │
│    CommandeService, DetailCommandeService, LocationService          │
│    CartService, StripeService, EmailService, PdfReceiptService      │
│    StockAlertService, StatisticsService, UtilisateurService         │
│    PaymentNotificationService                                      │
│                                                                    │
│  GestionEvenement/                                                 │
│    EvenementService, ParticipationService, AccompagnantService      │
│    UtilisateurService, PersonneService, StatistiquesService         │
├────────────────────────────────────────────────────────────────────┤
│                       ENTITÉS                                      │
│                                                                    │
│  GestionMarketplace/                                               │
│    Equipement, Vehicule, Terrain, Categorie, Fournisseur            │
│    Commande, DetailCommande, AchatFournisseur, Location             │
│    CartItem, Utilisateur                                            │
│    Enums: ProductType, PaymentStatus, DeliveryStatus, RentalStatus  │
│                                                                    │
│  GestionEvenement/                                                 │
│    Evenement, Participation, Accompagnant, Utilisateur, Personne    │
│    Enums: Type, Statut, Statutevent, Role                           │
├────────────────────────────────────────────────────────────────────┤
│                   OUTILS / CONNEXION                               │
│                                                                    │
│  GestionEvenement/                                                 │
│    MyConnection (Singleton DB), SessionManager                      │
│    EmailService, QRCodeUtil, TicketServerService                    │
│    WeatherService, HuggingFaceImageService                          │
│                                                                    │
│  GestionMarketplace/                                               │
│    DB_connection (délègue à MyConnection), MapPicker (Leaflet)      │
└────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Sécurité & Configuration

⚠️ **CRITIQUE — À LIRE AVANT DE DÉMARRER** ⚠️

FIRMA utilise plusieurs API externes (Google Maps, Stripe, Hugging Face, Gmail). Vous DEVEZ configurer vos propres clés API avant de pouvoir lancer l'application.

### Vérification rapide : Configuration complète en 3 étapes

#### **Étape 1️⃣ : Setup Initial (Une seule fois)**

**Windows (PowerShell):**
```powershell
.\setup-security.ps1
```

**Linux/Mac (Bash):**
```bash
bash setup-security.sh
```

Cela va :
- ✅ Créer `config.properties` depuis le template
- ✅ Vérifier que les secrets sont dans `.gitignore`
- ✅ Vous guider pour obtenir vos API keys

#### **Étape 2️⃣ : Remplir vos API Keys**

Éditez `src/main/resources/config.properties` :

```bash
# Linux/Mac
nano src/main/resources/config.properties

# Windows PowerShell
notepad src\main\resources\config.properties
```

Remplacez tous les `REPLACE_WITH_YOUR_*` par vos vraies clés :

```properties
# 📍 Google Maps (pour MapPicker de localisation)
google.maps.api.key=AIzaSy...YOUR_KEY...

# 🤖 Hugging Face (pour génération d'images IA)
huggingface.api.token=hf_...YOUR_TOKEN...

# 💳 Stripe (pour paiements en ligne)
stripe.secret.key=sk_...YOUR_KEY...
stripe.public.key=pk_...YOUR_KEY...

# 🗄️ Base de données
db.password=your_mysql_password

# 📧 Gmail (pour envoi d'emails)
email.smtp.password=xxxx xxxx xxxx xxxx  # (App Password Gmail)
```

#### **Étape 3️⃣ : Vérifier que les secrets sont ignorés par Git**

```bash
git status | grep config.properties
# Devrait afficher: RIEN (pas de sortie)
```

Si cela affiche `modified: src/main/resources/config.properties`, vous avez un problème !  
Voir la section [Secrets Accidentellement Commis](#secrets-accidentellement-commis).

### 🔑 Où obtenir vos API Keys

#### **1. Google Maps API Key**
- Allez à: https://console.cloud.google.com/
- Créez un nouveau projet
- Activez: `Maps JavaScript API` + `Geocoding API`
- Créez une clé API
- Copiez la clé dans `config.properties`

#### **2. Hugging Face Token**
- Allez à: https://huggingface.co/settings/tokens
- Créez un nouveau token (type: "read")
- Copiez le token (commence par `hf_`)

#### **3. Stripe Keys**
- Allez à: https://dashboard.stripe.com/apikeys
- Copiez la clé **Publishable** (`pk_...`)
- Copiez la clé **Secret** (`sk_...`) — 🔴 GARDEZ-LA SECRÈTE!

#### **4. Gmail App Password**
- Allez à: https://myaccount.google.com/apppasswords
- Sélectionnez "Mail" et "Windows Computer"
- Google génère un mot de passe 16 caractères
- Copiez-le (ce n'est PAS votre mot de passe Gmail!)

### ⚠️ Secrets Accidentellement Commis

Si vous voyez `config.properties` dans Git (❌ MAUVAIS) :

```bash
# Supprimer du Git mais garder le fichier local
git rm --cached src/main/resources/config.properties

# Commiter cette suppression
git commit -m "security: Remove config.properties with secrets from Git"

# Pusher
git push origin main

# 🔴 PUIS: Régénérez TOUTES vos clés API!
# (La clé Google, le token Hugging Face, les clés Stripe, etc.)
```

### 🛡️ Bonnes Pratiques de Sécurité

✅ **À FAIRE:**
- Utilisez `ConfigLoader.get("clé")` pour charger la configuration
- Mettez à jour `.gitignore` régulièrement
- Stockez les secrets dans les variables d'environnement (production)
- Demandez les credentials à l'équipe DevOps

❌ **À NE PAS FAIRE:**
- ❌ Hardcoder les clés API dans le code Java
- ❌ Commiter `config.properties` ou `.env`
- ❌ Partager les credentials par email
- ❌ Utiliser les mêmes clés pour dev/prod
- ❌ Laisser les clés dans les commentaires

### 📚 Documentation Complète

Pour plus de détails, voir [**SECURITY.md**](SECURITY.md) :
- Configuration avancée
- Variables d'environnement
- Secrets management
- Rotation de credentials
- Intégration CI/CD

---

## Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.8+**
- **MySQL 8.x** ou **MariaDB 10.4+**
- **API Keys** (voir section [🔐 Sécurité & Configuration](#-sécurité--configuration) ci-dessus)
- Un IDE Java (IntelliJ IDEA, Eclipse, VS Code…)

---

## Installation et Configuration

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/<votre-username>/firma.git
   cd firma
   ```

2. **Configurer la base de données** :
   Modifiez les paramètres de connexion dans `src/main/java/Firma/tools/GestionEvenement/MyConnection.java` :
   ```java
   private String url = "jdbc:mysql://localhost:3306/firma";
   private String user = "root";
   private String password = "";
   ```
   > `DB_connection` (Marketplace) délègue automatiquement à `MyConnection` — une seule connexion partagée.

3. **Installer les dépendances** :
   ```bash
   mvn clean install
   ```

---

## Base de données

1. **Créer la base de données** :
   ```sql
   CREATE DATABASE firma;
   ```

2. **Importer le schéma** :
   ```bash
   mysql -u root -p firma < src/main/java/Firma/database/firma.sql
   ```

3. **Appliquer les migrations** :
   ```bash
   mysql -u root -p firma < migration_code_participation.sql
   mysql -u root -p firma < src/main/java/Firma/database/migration_accompagnants.sql
   mysql -u root -p firma < src/main/java/Firma/database/migration_integration.sql
   ```

### Tables principales

| Module         | Table                | Description                                          |
|----------------|----------------------|------------------------------------------------------|
| **Événements** | `evenements`         | Événements avec capacité, lieu, dates, statut        |
|                | `participation`      | Inscriptions des utilisateurs aux événements          |
|                | `accompagnant`       | Accompagnants rattachés aux participations            |
| **Marketplace**| `equipements`        | Équipements agricoles en vente                        |
|                | `vehicules`          | Véhicules disponibles à la location                   |
|                | `terrains`           | Terrains agricoles disponibles à la location          |
|                | `categories`         | Catégories (équipements, véhicules, terrains)         |
|                | `fournisseurs`       | Fournisseurs d'équipements                            |
|                | `achats_fournisseurs`| Achats auprès des fournisseurs                        |
|                | `commandes`          | Commandes clients (paiement + livraison)              |
|                | `details_commandes`  | Lignes de commande (équipement, quantité, prix)       |
|                | `locations`          | Locations de véhicules et terrains                    |
| **Commun**     | `utilisateur`        | Comptes utilisateurs (admin / client)                 |

---

## Lancement

```bash
mvn clean javafx:run
```

Ou depuis votre IDE, exécutez la classe principale :
```
Firma.test.MainFX
```

### Flux de navigation

```
Login (742×480)
  ├─ Admin → AdminDashboard (plein écran)
  │    ├─ Accueil (KPIs + Charts + Raccourcis)
  │    ├─ Marketplace (catalogue admin)
  │    ├─ Équipements / Véhicules / Terrains (CRUD)
  │    ├─ Fournisseurs (CRUD)
  │    ├─ Commandes / Locations (suivi)
  │    ├─ Événements (Dashboard + CRUD)
  │    └─ Déconnexion → Login (fenêtre réduite)
  │
  └─ Client → ClientDashboard (plein écran)
       ├─ Accueil (Bienvenue FIRMA + raccourcis)
       ├─ Marketplace (parcourir, détails produit)
       ├─ Panier + Paiement Stripe
       ├─ Mes Locations
       ├─ Événements (parcourir, s'inscrire, tickets)
       ├─ Mes Participations
       └─ Déconnexion → Login (fenêtre réduite)
```

### Comptes de test

| Rôle          | Email                | Mot de passe |
|---------------|----------------------|--------------|
| Administrateur| *(à insérer en BDD)* | *(à définir)*|
| Utilisateur   | *(à insérer en BDD)* | *(à définir)*|

---

## Tests

Le projet inclut des tests unitaires JUnit 5 et des tests property-based (jqwik) :

```bash
mvn test
```

### Classes de test

| Dossier         | Classe                         | Couverture                                           |
|-----------------|--------------------------------|------------------------------------------------------|
| **Services**    | `EvenementServiceTest`         | CRUD complet + mise à jour statut                    |
|                 | `ParticipationServiceTest`     | CRUD + accompagnants + code participation            |
|                 | `AccompagnantServiceTest`      | CRUD + batch + réattribution                         |
| **Tools**       | `JavaFXPreservationTest`       | Vérification préservation JavaFX WebView             |
|                 | `MapPickerBugConditionTest`    | Tests conditions du MapPicker                        |
|                 | `SimpleWebViewTest`            | Tests WebView basiques                               |

> ⚠️ Les tests nécessitent une connexion active à la base de données MySQL locale.

---

## Structure du projet

```
firma/
├── pom.xml                                    # Configuration Maven + dépendances
├── README.md                                  # Ce fichier
├── migration_code_participation.sql           # Migration SQL
│
├── src/main/java/Firma/
│   │
│   ├── controllers/
│   │   ├── GestionEvenement/                  # Contrôleurs événements
│   │   │   ├── LoginController.java                  # Authentification + routage par rôle
│   │   │   ├── AccueilController.java                # Dashboard accueil événements
│   │   │   ├── EvenementController.java              # Dashboard admin événements
│   │   │   ├── FrontController.java                  # Interface visiteur événements
│   │   │   ├── DashboardAnalytique.java              # Charts et statistiques
│   │   │   ├── FormulaireCreationModificationEvenement.java
│   │   │   ├── ConstructionCartesEvenement.java      # Cartes événements (admin)
│   │   │   ├── ConstructionCartesVisiteur.java       # Cartes événements (visiteur)
│   │   │   ├── GestionParticipationsVisiteur.java    # Inscriptions visiteur
│   │   │   ├── AffichageListeParticipants.java       # Liste participants (admin)
│   │   │   ├── AffichageTicketsEtExportPDF.java      # Tickets + export PDF
│   │   │   └── OutilsInterfaceGraphique.java         # Utilitaires UI partagés
│   │   │
│   │   └── GestionMarketplace/                # Contrôleurs marketplace
│   │       ├── AdminDashboardController.java         # Dashboard admin principal
│   │       ├── AdminAccueilController.java           # Accueil admin (KPIs + charts)
│   │       ├── ClientDashboardController.java        # Dashboard client principal
│   │       ├── ClientAccueilController.java          # Page d'accueil client FIRMA
│   │       ├── ClientMarketplaceController.java      # Catalogue client
│   │       ├── ProductDetailController.java          # Détail produit
│   │       ├── CartPanelController.java              # Panier d'achat
│   │       ├── PaymentController.java                # Paiement Stripe
│   │       ├── RentalsPanelController.java           # Mes locations (client)
│   │       ├── EquipementController.java             # CRUD équipements (admin)
│   │       ├── VehiculeController.java               # CRUD véhicules (admin)
│   │       ├── TerrainController.java                # CRUD terrains (admin)
│   │       ├── FournisseurController.java            # CRUD fournisseurs (admin)
│   │       ├── CommandeAdminController.java          # Gestion commandes (admin)
│   │       ├── LocationAdminController.java          # Gestion locations (admin)
│   │       └── MarketplaceController.java            # Catalogue admin
│   │
│   ├── entities/
│   │   ├── GestionEvenement/                  # Entités événements
│   │   │   ├── Evenement.java                        # Événement (20 champs)
│   │   │   ├── Participation.java                    # Inscription (9 champs)
│   │   │   ├── Accompagnant.java                     # Accompagnant (4 champs)
│   │   │   ├── Utilisateur.java                      # Utilisateur (10 champs)
│   │   │   ├── Personne.java                         # Entité minimale (id, nom, prénom)
│   │   │   └── Enums: Type, Statut, Statutevent, Role
│   │   │
│   │   └── GestionMarketplace/                # Entités marketplace
│   │       ├── Equipement.java                       # Équipement agricole
│   │       ├── Vehicule.java                         # Véhicule de location
│   │       ├── Terrain.java                          # Terrain agricole
│   │       ├── Categorie.java                        # Catégorie produit
│   │       ├── Fournisseur.java                      # Fournisseur
│   │       ├── Commande.java                         # Commande client
│   │       ├── DetailCommande.java                   # Ligne de commande
│   │       ├── AchatFournisseur.java                 # Achat fournisseur
│   │       ├── Location.java                         # Location véhicule/terrain
│   │       ├── CartItem.java                         # Article panier (en mémoire)
│   │       ├── Utilisateur.java                      # Utilisateur marketplace
│   │       └── Enums: ProductType, PaymentStatus, DeliveryStatus, RentalStatus
│   │
│   ├── services/
│   │   ├── GestionEvenement/                  # Services événements
│   │   │   ├── EvenementService.java                 # CRUD événements + gestion places
│   │   │   ├── ParticipationService.java             # CRUD participations + codes
│   │   │   ├── AccompagnantService.java              # CRUD accompagnants
│   │   │   ├── UtilisateurService.java               # CRUD utilisateurs
│   │   │   ├── PersonneService.java                  # CRUD personnes
│   │   │   └── StatistiquesService.java              # KPIs, distributions, séries temporelles
│   │   │
│   │   └── GestionMarketplace/                # Services marketplace
│   │       ├── EquipementService.java                # CRUD équipements
│   │       ├── VehiculeService.java                  # CRUD véhicules
│   │       ├── TerrainService.java                   # CRUD terrains
│   │       ├── CategorieService.java                 # CRUD catégories
│   │       ├── FournisseurService.java               # CRUD fournisseurs
│   │       ├── AchatFournisseurService.java          # CRUD achats
│   │       ├── CommandeService.java                  # CRUD commandes
│   │       ├── DetailCommandeService.java            # CRUD lignes commande
│   │       ├── LocationService.java                  # CRUD locations
│   │       ├── CartService.java                      # Panier (singleton) + checkout
│   │       ├── StripeService.java                    # Paiement Stripe (test mode)
│   │       ├── EmailService.java                     # Emails confirmation + alertes
│   │       ├── PdfReceiptService.java                # Génération reçus PDF (iText 7)
│   │       ├── StockAlertService.java                # Alertes stock par email
│   │       ├── StatisticsService.java                # KPIs + charts marketplace
│   │       ├── UtilisateurService.java               # CRUD utilisateurs
│   │       └── PaymentNotificationService.java       # Notifications paiement
│   │
│   ├── interfaces/
│   │   ├── GestionEvenement/IService.java            # Interface CRUD générique
│   │   └── GestionMarketplace/IService.java          # Interface CRUD générique
│   │
│   ├── tools/
│   │   ├── GestionEvenement/
│   │   │   ├── MyConnection.java                     # Connexion DB (Singleton)
│   │   │   ├── SessionManager.java                   # Session utilisateur courante
│   │   │   ├── EmailService.java                     # Emails événements + tickets PDF
│   │   │   ├── QRCodeUtil.java                       # Génération QR codes (ZXing)
│   │   │   ├── TicketServerService.java              # Serveur HTTP tickets QR (port 8642)
│   │   │   ├── WeatherService.java                   # Météo Open-Meteo (async)
│   │   │   └── HuggingFaceImageService.java          # Génération images IA (SDXL)
│   │   │
│   │   └── GestionMarketplace/
│   │       ├── DB_connection.java                    # Adaptateur DB (délègue à MyConnection)
│   │       └── MapPicker.java                        # Sélecteur d'adresse Leaflet (WebView)
│   │
│   ├── database/
│   │   ├── firma.sql                                 # Schéma complet de la BDD
│   │   ├── migration_accompagnants.sql               # Migration accompagnants
│   │   └── migration_integration.sql                 # Migration intégration modules
│   │
│   └── test/
│       ├── MainFX.java                               # Point d'entrée JavaFX
│       └── Main.java                                 # Tests console
│
├── src/main/resources/
│   ├── LoginApplication.fxml                         # Vue login
│   │
│   ├── GestionEvenement/                             # Vues événements
│   │   ├── Accueil.fxml                              # Accueil événements
│   │   ├── Dashboard.fxml                            # Dashboard admin événements
│   │   ├── DashboardContent.fxml                     # Contenu dashboard
│   │   ├── front.fxml                                # Interface visiteur
│   │   ├── FrontContent.fxml                         # Contenu front visiteur
│   │   ├── evenement-style.css                       # Styles événements
│   │   └── front-style.css                           # Styles visiteur
│   │
│   ├── GestionMarketplace/marketplace/GUI/
│   │   ├── views/                                    # Vues marketplace
│   │   │   ├── AdminDashboard.fxml                   # Dashboard admin
│   │   │   ├── AdminAccueilContent.fxml              # Accueil admin (KPIs + charts)
│   │   │   ├── client_dashboard.fxml                 # Dashboard client
│   │   │   ├── ClientAccueilView.fxml                # Page d'accueil client
│   │   │   ├── ClientMarketplaceView.fxml            # Catalogue client
│   │   │   ├── ProductDetailView.fxml                # Détail produit
│   │   │   ├── CartPanelView.fxml                    # Panier
│   │   │   ├── PaymentView.fxml                      # Paiement
│   │   │   ├── RentalsPanelView.fxml                 # Mes locations
│   │   │   ├── EquipementView.fxml                   # CRUD équipements
│   │   │   ├── VehiculeView.fxml                     # CRUD véhicules
│   │   │   ├── TerrainView.fxml                      # CRUD terrains
│   │   │   ├── FournisseurView.fxml                  # CRUD fournisseurs
│   │   │   ├── CommandeAdminView.fxml                # Commandes admin
│   │   │   ├── LocationAdminView.fxml                # Locations admin
│   │   │   └── MarketplaceView.fxml                  # Catalogue admin
│   │   ├── css/styles.css                            # Styles marketplace
│   │   └── leaflet/                                  # Bibliothèque Leaflet (maps)
│   │
│   └── image/                                        # Logos, images événements, IA
│
├── src/test/java/Firma/
│   ├── services/                                     # Tests unitaires services
│   │   ├── EvenementServiceTest.java
│   │   ├── ParticipationServiceTest.java
│   │   └── AccompagnantServiceTest.java
│   │
│   └── tools/GestionMarketplace/                     # Tests outils
│       ├── JavaFXPreservationTest.java
│       ├── MapPickerBugConditionTest.java
│       └── SimpleWebViewTest.java
│
└── receipts/                                         # Reçus PDF générés
```

---

## Fonctionnalités

### 🔐 Authentification
- Connexion par email / mot de passe avec validation regex
- Routage automatique par rôle : admin → Dashboard Admin, client → Dashboard Client
- Gestion de session via `SessionManager` (singleton)
- Déconnexion avec nettoyage de session et retour à la fenêtre de login

### 🏠 Pages d'Accueil
- **Admin** : Tableau de bord avec KPIs (événements, participations, commandes, revenus), graphiques (PieChart, BarChart), classement top événements, raccourcis vers chaque module
- **Client** : Page de bienvenue personnalisée FIRMA avec date du jour, raccourcis visuels vers les services (Marketplace, Événements, Locations, Participations), sections de présentation, pied de page contact

---

### 🛒 Module Marketplace

#### Côté Admin
- **Équipements** : CRUD complet, gestion de stock, seuil d'alerte, upload d'image
- **Véhicules** : CRUD complet, tarifs par jour/semaine/mois, immatriculation
- **Terrains** : CRUD complet, superficie, tarifs mensuel/annuel, caution
- **Catégories** : Organisation par type de produit (Équipement, Véhicule, Terrain)
- **Fournisseurs** : CRUD complet, suivi des achats, statut actif/inactif
- **Commandes** : Suivi des commandes (paiement, livraison), détail des lignes
- **Locations** : Suivi des locations (véhicules + terrains), statuts de réservation
- **Statistiques** : Revenus mensuels, répartition par catégorie, statuts de livraison, valeur totale du stock

#### Côté Client
- **Catalogue** : Navigation dans les produits avec cartes visuelles et détail
- **Panier** : Ajout d'équipements (achat) et véhicules/terrains (location), modification quantités et dates
- **Paiement Stripe** : Paiement sécurisé par carte bancaire (mode test), détection Visa/Mastercard
- **Reçus PDF** : Génération automatique de reçus professionnels avec branding FIRMA (iText 7)
- **Email confirmation** : Envoi automatique du reçu PDF en pièce jointe après paiement
- **Alertes de stock** : Notification email automatique à l'admin quand le stock passe sous le seuil
- **Mes Locations** : Consultation et suivi des véhicules/terrains loués
- **Sélection d'adresse** : Carte interactive Leaflet avec géocodage inversé (Nominatim)

---

### 📅 Module Gestion d'Événements

#### Côté Admin
- **CRUD événements** : Créer, modifier, supprimer, annuler des événements
- **Formulaires validés** : Titre, description, dates, horaires, capacité, lieu, contact
- **Upload d'image** : Image personnalisée ou **génération IA** (Hugging Face Stable Diffusion XL)
- **Recherche et tri** : Par titre, date, capacité, lieu
- **Participants** : Liste détaillée avec accompagnants, statuts, codes de participation
- **Dashboard analytique** : KPIs, répartition par type (PieChart), participations par mois (BarChart), top événements

#### Côté Client (Visiteur)
- **Parcourir les événements** : Cartes visuelles avec jauge de places disponibles
- **S'inscrire** : Inscription avec ajout dynamique d'accompagnants
- **Modifier / Annuler** sa participation
- **Tickets PDF** : Génération avec code unique (`PART-XXXXX`) et QR codes pour chaque personne
- **QR Codes** : Scan pour accéder à une page ticket mobile (serveur HTTP embarqué sur le réseau local)
- **Email de confirmation** : Lien cliquable pour confirmer la participation → envoi automatique du ticket PDF
- **Météo** : Prévisions météo pour le lieu et la date de l'événement (Open-Meteo)
- **Google Maps** : Ouverture de l'adresse de l'événement dans le navigateur

---

### 📄 Génération de Documents
- **Tickets PDF** (événements) : Code unique, QR code par participant/accompagnant, design brandé
- **Reçus PDF** (marketplace) : Récapitulatif commande, tableau d'articles, totaux, branding FIRMA
- Export via iText 7 avec formatage professionnel

### 📧 Système d'Emails
- **Événements** : Email de confirmation avec lien cliquable + email ticket PDF en pièce jointe
- **Marketplace** : Email de confirmation de paiement avec reçu PDF + alertes stock
- Envoi asynchrone via Jakarta Mail (SMTP Gmail)

### 📊 Tableaux de Bord & Statistiques
- **Admin Événements** : Nombre d'événements, participations confirmées/en attente, taux de remplissage, répartition par type, participations par mois, top événements, événements cette semaine
- **Admin Marketplace** : Total équipements/véhicules/terrains/fournisseurs/commandes/locations, revenus totaux, valeur du stock, revenus mensuels, répartition par catégorie, statuts de livraison

---

## Captures d'écran

> *À ajouter : captures du login, dashboard admin, dashboard client, catalogue, paiement, événements, tickets…*

---

## Auteurs

- **Naama**/ **Hamza** — Développeur principal
- Projet académique — **ESPRIT** (3A7)

---

## Licence

Projet académique — usage éducatif uniquement.
