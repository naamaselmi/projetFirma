<p align="center">
  <img src="src/main/resources/image/logo.png" alt="FIRMA Logo" width="120"/>
</p>

<h1 align="center">🌾 FIRMA</h1>
<h3 align="center">Plateforme Intelligente de Gestion Agricole</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/JavaFX-20.0.2-blue?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Stripe-Payments-635BFF?style=for-the-badge&logo=stripe&logoColor=white" alt="Stripe"/>
  <img src="https://img.shields.io/badge/AI_Powered-HuggingFace-FFD21E?style=for-the-badge&logo=huggingface&logoColor=black" alt="AI"/>
</p>

<p align="center">
  Application de bureau JavaFX complète intégrant <strong>5 modules métier</strong> — Marketplace, Événements, Techniciens, Forum & Utilisateurs — dans une interface unifiée avec authentification par rôle.
</p>

<p align="center">
  <a href="#-aperçu">Aperçu</a> •
  <a href="#-technologies">Technologies</a> •
  <a href="#-modules--fonctionnalités">Modules</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-lancement">Lancement</a> •
  <a href="#-tests">Tests</a> •
  <a href="#-équipe">Équipe</a>
</p>

---

## 📖 Aperçu

**FIRMA** est une application Java/JavaFX développée dans le cadre d'un projet académique à **ESPRIT (3A)**. Elle offre une suite complète d'outils pour la gestion d'une entreprise agricole, couvrant :

| # | Module | Description |
|:-:|--------|-------------|
| 🛒 | **Marketplace** | Catalogue de produits agricoles, commandes, paiements Stripe, locations & reçus PDF |
| 📅 | **Événements** | Création d'événements, inscriptions, tickets PDF avec QR codes, météo & images IA |
| 🔧 | **Techniciens** | Demandes de service, diagnostic IA, assignation automatique, géolocalisation temps réel |
| 💬 | **Forum** | Espace de discussion communautaire pour les utilisateurs |
| 👤 | **Utilisateurs** | Gestion des comptes, authentification et profils |

Chaque module possède un **tableau de bord** (admin & client) avec statistiques, graphiques et raccourcis de navigation.

---

## 🛠 Technologies

<table>
<tr>
<td>

### Core
| Composant | Technologie |
|-----------|-------------|
| Langage | Java 17 |
| Interface | JavaFX 20.0.2 + FXML + CSS |
| Base de données | MySQL 8.x / MariaDB 10.4 |
| Accès DB | JDBC (MySQL Connector 8.0.30) |
| Build | Maven 3.8+ |

</td>
<td>

### Intégrations
| Composant | Technologie |
|-----------|-------------|
| Paiement | Stripe Java SDK 24.0.0 |
| PDF | iText 7.2.5 |
| QR Codes | ZXing 3.5.2 |
| Email | Jakarta Mail 2.0.1 |
| JSON | Gson 2.10.1 + Jackson 2.15.2 |

</td>
</tr>
<tr>
<td>

### APIs Externes
| Composant | Technologie |
|-----------|-------------|
| IA (images) | HuggingFace Inference API (SDXL) |
| IA (diagnostic) | HuggingFace (T5, GPT-2, Zephyr) |
| Cartographie | Leaflet.js + OpenStreetMap |
| Géocodage | Nominatim (OpenStreetMap) |
| Routes | OSRM (Open Source Routing) |
| Météo | Open-Meteo API |
| Géoloc IP | ipwho.is |

</td>
<td>

### Tests & Outils
| Composant | Technologie |
|-----------|-------------|
| Tests unitaires | JUnit 5.10.2 |
| Property-based | jqwik 1.8.2 |
| HTTP Client | OkHttp 4.12.0 |
| Serveur QR | JDK HttpServer embarqué |
| Logging | SLF4J 1.7.36 |

</td>
</tr>
</table>

---

## 🧩 Modules & Fonctionnalités

### 🛒 Module Marketplace
> *Gestion complète du catalogue agricole, des commandes et des paiements*

<details>
<summary><b>👨‍💼 Côté Admin</b></summary>

- **Équipements** — CRUD complet, gestion de stock avec seuils d'alerte, upload d'images
- **Véhicules** — CRUD complet, tarifs jour/semaine/mois, gestion d'immatriculations
- **Terrains** — CRUD complet, superficie, tarifs mensuel/annuel, caution
- **Catégories** — Organisation par type de produit (Équipement, Véhicule, Terrain)
- **Fournisseurs** — CRUD complet, suivi des achats, statut actif/inactif
- **Commandes** — Suivi des paiements et livraisons, détail par ligne
- **Locations** — Suivi des réservations véhicules & terrains
- **Statistiques** — Revenus mensuels, répartition par catégorie, valeur totale du stock

</details>

<details>
<summary><b>🛍 Côté Client</b></summary>

- **Catalogue** — Navigation visuelle avec cartes produits et page de détail
- **Panier** — Ajout d'équipements (achat) et véhicules/terrains (location)
- **Paiement Stripe** — Paiement sécurisé par carte (mode test), détection Visa/Mastercard
- **Reçus PDF** — Génération automatique avec branding FIRMA (iText 7)
- **Email de confirmation** — Reçu PDF en pièce jointe après paiement
- **Alertes de stock** — Notification email automatique quand le stock passe sous le seuil
- **Mes Locations** — Consultation et suivi des véhicules/terrains loués
- **Carte interactive** — Sélection d'adresse via Leaflet avec géocodage inversé (Nominatim)

</details>

---

### 📅 Module Gestion d'Événements
> *Organisation et participation aux événements agricoles*

<details>
<summary><b>👨‍💼 Côté Admin</b></summary>

- **CRUD événements** — Créer, modifier, supprimer, annuler
- **Formulaires validés** — Titre, description, dates, horaires, capacité, lieu, contact
- **Upload d'image** — Image personnalisée ou **génération IA** (HuggingFace Stable Diffusion XL)
- **Recherche & tri** — Par titre, date, capacité, lieu
- **Participants** — Liste détaillée avec accompagnants, statuts, codes de participation
- **Dashboard analytique** — KPIs, PieChart par type, BarChart participations/mois, top événements

</details>

<details>
<summary><b>🎫 Côté Client (Visiteur)</b></summary>

- **Parcourir** — Cartes visuelles avec jauge de places disponibles
- **S'inscrire** — Inscription avec ajout dynamique d'accompagnants
- **Modifier / Annuler** — Gestion de sa participation
- **Tickets PDF** — Code unique (`PART-XXXXX`) + QR code par participant/accompagnant
- **QR Codes** — Scan pour page ticket mobile (serveur HTTP embarqué sur réseau local)
- **Email de confirmation** — Lien cliquable → confirmation → envoi automatique du ticket PDF
- **Météo intégrée** — Prévisions pour le lieu et la date de l'événement (Open-Meteo)
- **Google Maps** — Ouverture de l'adresse dans le navigateur

</details>

---

### 🔧 Module Gestion des Techniciens
> *Demandes de service, diagnostic intelligent et gestion des interventions*

<details>
<summary><b>👨‍💼 Côté Admin (Back-office)</b></summary>

- **CRUD Techniciens** — Ajout, modification, suppression, profils complets (spécialité, CIN, localisation)
- **Gestion des demandes** — Suivi de toutes les demandes de service, assignation aux techniciens
- **Assignation automatique** — Algorithme multi-critères pondéré sur 100 points :

  | Critère | Poids | Description |
  |---------|:-----:|-------------|
  | Compétence | 30 | Correspondance de la spécialité |
  | Disponibilité | 25 | Charge de travail journalière (max 6/jour) |
  | Proximité | 20 | Distance Haversine (rayon 50 km) |
  | Note moyenne | 15 | Moyenne des avis clients |
  | Expérience | 10 | Nombre de demandes complétées |

- **Carte Snapchat** — Carte temps réel des techniciens actifs (Leaflet + OSRM)
- **Notifications email** — Confirmation client + assignation technicien automatique

</details>

<details>
<summary><b>🧑‍🌾 Côté Client (Front)</b></summary>

- **Soumettre une demande** — Formulaire avec type de problème, description, adresse
- **Diagnostic IA** — Deux modes :
  - **Hors ligne** : Analyse par mots-clés → catégorie, solutions, urgence, score de confiance
  - **En ligne** : Prompt vers HuggingFace (T5 / GPT-2 / Zephyr-7B) avec fallback automatique
- **Liste des techniciens** — Parcourir les techniciens disponibles, filtrer par spécialité
- **Avis & notations** — Laisser un avis (note 1–10 + commentaire) après intervention
- **Détail de la demande** — Suivi du statut, technicien assigné, historique
- **Géolocalisation** — Calcul de distance et itinéraire vers le technicien assigné

</details>

---

### 💬 Module Forum
> *Espace communautaire de discussion et d'entraide entre utilisateurs*

- Création et gestion de sujets de discussion
- Réponses et interactions entre membres de la communauté
- Modération et gestion du contenu

---

### 👤 Module Gestion Utilisateurs
> *Authentification, profils et gestion des comptes*

- Connexion par email / mot de passe avec validation
- Routage automatique par rôle : admin → Dashboard Admin, client → Dashboard Client
- Gestion de session via `SessionManager` (singleton)
- Gestion des profils utilisateurs
- Déconnexion avec nettoyage de session

---

## 🏗 Architecture

Le projet suit le pattern **MVC** (Modèle-Vue-Contrôleur) organisé en 5 modules :

```
firma/
│
├── 📦 src/main/java/Firma/
│   │
│   ├── 🎮 controllers/
│   │   ├── GestionEvenement/          # 11 contrôleurs événements
│   │   ├── GestionMarketplace/        # 17 contrôleurs marketplace
│   │   └── GestionTechnicien/         # 12 contrôleurs techniciens
│   │
│   ├── 📊 entities/
│   │   ├── GestionEvenement/          # Evenement, Participation, Accompagnant...
│   │   ├── GestionMarketplace/        # Equipement, Vehicule, Terrain, Commande...
│   │   └── GestionTechnicien/         # Technicien, Demande, Avis, Coordonnees
│   │
│   ├── ⚙️ services/
│   │   ├── GestionEvenement/          # EvenementService, ParticipationService...
│   │   ├── GestionMarketplace/        # StripeService, CartService, PdfReceiptService...
│   │   └── GestionTechnicien/         # DiagnosticIAService, AutoAssignationService...
│   │
│   ├── 📐 interfaces/                 # IService<T> — interface CRUD générique
│   │
│   ├── 🔧 tools/
│   │   ├── ConfigLoader.java          # Chargement centralisé config.properties
│   │   ├── GestionEvenement/          # MyConnection, SessionManager, QRCodeUtil...
│   │   ├── GestionMarketplace/        # DB_connection, MapPicker (Leaflet)
│   │   └── GestionTechnicien/         # EmailConfig, GoogleMapsConfig
│   │
│   ├── 🗄 database/                   # Scripts SQL (schéma + migrations)
│   └── 🚀 test/                       # MainFX.java (point d'entrée)
│
├── 🎨 src/main/resources/
│   ├── LoginApplication.fxml
│   ├── GestionEvenement/              # 7 FXML + 2 CSS
│   ├── GestionMarketplace/            # 16 FXML + CSS + Leaflet
│   ├── GestionTechnicien/             # 12 FXML
│   ├── image/ & images/               # Assets visuels
│   └── config.properties              # 🔐 Clés API (non commité)
│
├── 🧪 src/test/java/Firma/            # Tests JUnit 5 + jqwik
└── 📄 receipts/                       # Reçus PDF générés
```

---

## 🔐 Sécurité & Configuration

> ⚠️ **IMPORTANT — À lire avant de démarrer**

FIRMA utilise plusieurs API externes. Vous **devez** configurer vos propres clés API avant de lancer l'application.

### Étape 1 — Configuration initiale

```powershell
# Windows PowerShell
.\setup-security.ps1
```
```bash
# Linux / macOS
bash setup-security.sh
```

### Étape 2 — Remplir vos API Keys

Éditez `src/main/resources/config.properties` :

```properties
# 📍 Google Maps (localisation)
google.maps.api.key=AIzaSy...YOUR_KEY...

# 🤖 Hugging Face (génération d'images & diagnostic IA)
huggingface.api.token=hf_...YOUR_TOKEN...

# 💳 Stripe (paiements en ligne)
stripe.secret.key=sk_...YOUR_KEY...
stripe.public.key=pk_...YOUR_KEY...

# 🗄️ Base de données
db.password=your_mysql_password

# 📧 Gmail (envoi d'emails)
email.smtp.password=xxxx xxxx xxxx xxxx
```

### Étape 3 — Vérifier le `.gitignore`

```bash
git status | grep config.properties
# Ne doit rien afficher (fichier ignoré par Git)
```

<details>
<summary><b>🔑 Où obtenir vos API Keys</b></summary>

| API | URL | Notes |
|-----|-----|-------|
| **Google Maps** | [console.cloud.google.com](https://console.cloud.google.com/) | Activer Maps JavaScript + Geocoding API |
| **Hugging Face** | [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens) | Token de type "read" (commence par `hf_`) |
| **Stripe** | [dashboard.stripe.com/apikeys](https://dashboard.stripe.com/apikeys) | Clés Publishable (`pk_`) et Secret (`sk_`) |
| **Gmail App Password** | [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) | Mot de passe 16 caractères (pas votre MDP Gmail) |

</details>

<details>
<summary><b>⚠️ Secrets accidentellement commis ?</b></summary>

```bash
# Supprimer du Git mais garder le fichier local
git rm --cached src/main/resources/config.properties
git commit -m "security: Remove config.properties with secrets from Git"
git push origin main

# 🔴 PUIS: Régénérez TOUTES vos clés API!
```

</details>

---

## 📥 Installation

### Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.8+**
- **MySQL 8.x** ou **MariaDB 10.4+**
- **API Keys** configurées (voir section ci-dessus)

### Mise en place

```bash
# 1. Cloner le dépôt
git clone https://github.com/<votre-username>/firma.git
cd firma

# 2. Installer les dépendances
mvn clean install
```

### Base de données

```sql
-- Créer la base de données
CREATE DATABASE firma;
```

```bash
# Importer le schéma
mysql -u root -p firma < src/main/java/Firma/database/firma.sql

# Appliquer les migrations
mysql -u root -p firma < migration_code_participation.sql
mysql -u root -p firma < src/main/java/Firma/database/migration_accompagnants.sql
mysql -u root -p firma < src/main/java/Firma/database/migration_integration.sql
```

<details>
<summary><b>📋 Tables principales</b></summary>

| Module | Table | Description |
|--------|-------|-------------|
| **Événements** | `evenements` | Événements avec capacité, lieu, dates, statut |
| | `participation` | Inscriptions des utilisateurs aux événements |
| | `accompagnant` | Accompagnants rattachés aux participations |
| **Marketplace** | `equipements` | Équipements agricoles en vente |
| | `vehicules` | Véhicules disponibles à la location |
| | `terrains` | Terrains agricoles à la location |
| | `categories` | Catégories produits |
| | `fournisseurs` | Fournisseurs d'équipements |
| | `achats_fournisseurs` | Achats fournisseurs |
| | `commandes` | Commandes clients |
| | `details_commandes` | Lignes de commande |
| | `locations` | Locations véhicules & terrains |
| **Techniciens** | `technicien` | Profils techniciens (spécialité, localisation) |
| | `demande` | Demandes de service client |
| | `avis` | Avis et notations (1–10) |
| **Commun** | `utilisateur` | Comptes utilisateurs (admin / client) |

</details>

---

## 🚀 Lancement

```bash
mvn clean javafx:run
```

Ou depuis votre IDE, exécutez la classe principale : `Firma.test.MainFX`

### Flux de navigation

```
🔐 Login
 │
 ├─── 👨‍💼 Admin ──────────────────────────────────────
 │     ├── 🏠 Accueil (KPIs + Charts + Raccourcis)
 │     ├── 🛒 Marketplace (Équipements, Véhicules, Terrains)
 │     ├── 📦 Fournisseurs & Commandes & Locations
 │     ├── 📅 Événements (Dashboard + CRUD)
 │     ├── 🔧 Techniciens (Gestion + Carte temps réel)
 │     ├── 💬 Forum (Modération)
 │     └── 🚪 Déconnexion
 │
 └─── 🧑‍🌾 Client ─────────────────────────────────────
       ├── 🏠 Accueil (Bienvenue + raccourcis)
       ├── 🛍 Marketplace (Parcourir + Panier + Paiement)
       ├── 📄 Mes Locations
       ├── 📅 Événements (Parcourir + S'inscrire + Tickets)
       ├── 🔧 Demande Technicien + Diagnostic IA
       ├── 💬 Forum (Participer)
       └── 🚪 Déconnexion
```

---

## 🧪 Tests

```bash
mvn test
```

| Dossier | Classe | Couverture |
|---------|--------|------------|
| **Services** | `EvenementServiceTest` | CRUD complet + mise à jour statut |
| | `ParticipationServiceTest` | CRUD + accompagnants + code participation |
| | `AccompagnantServiceTest` | CRUD + batch + réattribution |
| **Tools** | `JavaFXPreservationTest` | Vérification préservation JavaFX WebView |
| | `MapPickerBugConditionTest` | Tests conditions du MapPicker |
| | `SimpleWebViewTest` | Tests WebView basiques |

> ⚠️ Les tests nécessitent une connexion active à la base de données MySQL locale.

---

## 👥 Équipe

> 🎓 Projet académique — **ESPRIT** (3A)

| | Membre | Module | Contact |
|:-:|--------|--------|---------|
| 📅 | **Naama Selmi** | Gestion Événements | [naama.selmi@esprit.tn](mailto:naama.selmi@esprit.tn) |
| 🛒 | **Hamza Slimani** | Gestion Marketplace | [hamza.slimani@esprit.tn](mailto:hamza.slimani@esprit.tn) |
| 🔧 | **Molka Ajengui** | Gestion Techniciens | [Molka.Ajengui@esprit.tn](mailto:Molka.Ajengui@esprit.tn) |
| 💬 | **Zeineb ElAbed** | Gestion Forum | [Zeineb.ElAbed@esprit.tn](mailto:Zeineb.ElAbed@esprit.tn) |
| 👤 | **Mehdi Obba** | Gestion Utilisateurs | [Mehdi.OBBA@esprit.tn](mailto:Mehdi.OBBA@esprit.tn) |

---
## 📄 Licence

Projet académique — usage éducatif uniquement.

---

<p align="center">
  <sub>Made with ❤️ by the FIRMA team at ESPRIT</sub>
</p>
