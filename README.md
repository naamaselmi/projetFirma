# 🌾 FIRMA — Plateforme de Gestion Agricole

> **Application de bureau JavaFX** pour la gestion complète d'une entreprise agricole.  
> Quatre modules métier — **Marketplace**, **Événements**, **Forum**, **Technicien** — réunis dans une interface unifiée avec gestion des utilisateurs et authentification par rôle.

<p align="center">
  <img src="src/main/resources/image/logo.png" alt="FIRMA Logo" width="180"/>
</p>

---

## 📋 Table des matières

| | |
|---|---|
| [Aperçu](#-aperçu) | [Technologies](#-technologies) |
| [Modules](#-modules) | [Architecture](#-architecture) |
| [Prérequis](#-prérequis) | [Installation](#-installation-et-configuration) |
| [Base de données](#-base-de-données) | [Lancement](#-lancement) |
| [Tests](#-tests) | [Structure du projet](#-structure-du-projet) |
| [Fonctionnalités détaillées](#-fonctionnalités-détaillées) | [Auteurs](#-auteurs) |

---

## 🔍 Aperçu

**FIRMA** est une application Java / JavaFX développée dans le cadre d'un projet académique à **ESPRIT** (3A7). Elle couvre l'ensemble des besoins d'une entreprise agricole à travers **quatre modules métier** et un **système de gestion des utilisateurs** :

| # | Module | Périmètre |
|:-:|--------|-----------|
| 1 | **Marketplace** | Catalogue produits (équipements, véhicules, terrains), commandes, paiements Stripe, locations, fournisseurs, alertes de stock, reçus PDF |
| 2 | **Gestion d'Événements** | Création d'événements, inscriptions & accompagnants, tickets PDF + QR codes, confirmation email, météo, génération d'images IA |
| 3 | **Forum** | Espace de discussion communautaire pour les agriculteurs — échange de conseils, questions techniques et retours d'expérience |
| 4 | **Technicien** | Gestion des interventions techniques, suivi de maintenance et planification des missions terrain |
| 5 | **Gestion Utilisateurs** | Inscription, authentification, profils, rôles (admin / client), gestion de session |

Chaque module dispose d'un **tableau de bord** dédié (admin & client) avec des KPIs, graphiques interactifs et raccourcis de navigation.

---

## 🛠 Technologies

| Composant            | Technologie                               |
|----------------------|-------------------------------------------|
| **Langage**          | Java 17                                   |
| **Interface**        | JavaFX 20.0.2 · FXML · CSS               |
| **Base de données**  | MySQL 8.x / MariaDB 10.4                 |
| **Accès DB**         | JDBC (MySQL Connector 8.0.30)             |
| **Build**            | Maven 3.8+                                |
| **PDF**              | iText 7.2.5                               |
| **Paiement**         | Stripe Java SDK 24.0.0                    |
| **Email**            | Jakarta Mail 2.0.1 (SMTP Gmail)           |
| **QR Codes**         | ZXing 3.5.2                               |
| **JSON**             | Gson 2.10.1                               |
| **Cartographie**     | Leaflet (WebView) · Nominatim             |
| **Météo**            | Open-Meteo API                            |
| **IA (images)**      | Hugging Face Inference API (SDXL)         |
| **Tests**            | JUnit 5.10.2 · jqwik 1.8.2               |
| **Web (QR tickets)** | Serveur HTTP embarqué (JDK HttpServer)    |

---

## 📦 Modules

### 🛒 1. Marketplace
Catalogue complet de produits agricoles avec gestion des commandes, paiements sécurisés et locations.

- **Admin** — CRUD équipements, véhicules, terrains · gestion fournisseurs & achats · suivi commandes & locations · alertes de stock · dashboard KPIs & graphiques
- **Client** — Catalogue visuel · détail produit · panier · paiement Stripe · reçus PDF par email · sélection d'adresse (carte Leaflet) · suivi de locations

### 📅 2. Gestion d'Événements
Organisation complète d'événements agricoles : conférences, salons, ateliers, journées portes ouvertes.

- **Admin** — CRUD événements · formulaires validés · génération d'images IA (Hugging Face SDXL) · dashboard analytique · gestion participants & accompagnants
- **Client** — Parcourir & s'inscrire · accompagnants · tickets PDF + QR codes · confirmation email · météo intégrée · Google Maps

### 💬 3. Forum
Espace communautaire dédié aux professionnels agricoles.

- Discussions thématiques · questions / réponses · partage de conseils · modération

### 🔧 4. Technicien
Module de gestion des interventions techniques sur le terrain.

- Planification de missions · suivi de maintenance · historique des interventions

### 👤 5. Gestion Utilisateurs
Système centralisé d'authentification et de gestion des profils.

- Inscription & connexion (email / mot de passe) · routage par rôle (admin / client) · gestion de session · profil utilisateur

---

## 🏗 Architecture

Le projet suit le pattern **MVC** (Modèle-Vue-Contrôleur) avec une organisation modulaire par domaine métier :

```
                    ┌──────────────────────────┐
                    │      🔐 LOGIN            │
                    │   LoginApplication.fxml   │
                    └────────┬─────────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
   ┌─────────────────┐          ┌─────────────────┐
   │   ADMIN PANEL   │          │  CLIENT PANEL   │
   │  AdminDashboard │          │ ClientDashboard  │
   └────────┬────────┘          └────────┬────────┘
            │                            │
   ┌────────┼────────┐         ┌────────┼────────┐
   │        │        │         │        │        │
   ▼        ▼        ▼         ▼        ▼        ▼
 Market   Events   Tech     Market   Events   Forum
  place             nicien    place           Profil
```

**Couches applicatives :**

| Couche | Rôle | Exemples |
|--------|------|----------|
| **Vue** (FXML + CSS) | Interface utilisateur | `AdminDashboard.fxml`, `front.fxml`, `PaymentView.fxml` |
| **Contrôleur** | Logique d'interaction | `EvenementController`, `PaymentController`, `ClientDashboardController` |
| **Service** (DAO) | Accès aux données & métier | `StripeService`, `EvenementService`, `CartService` |
| **Entité** | Modèle de données | `Evenement`, `Commande`, `Utilisateur` |
| **Outils** | Utilitaires transversaux | `MyConnection`, `EmailService`, `QRCodeUtil`, `WeatherService` |

---

## ⚙ Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| MySQL / MariaDB | 8.x / 10.4+ |
| IDE | IntelliJ IDEA, Eclipse ou VS Code |

---

## 📥 Installation et Configuration

```bash
# 1. Cloner le dépôt
git clone https://github.com/naamaselmi/projetFirma.git
cd projetFirma

# 2. Installer les dépendances
mvn clean install
```

**Configuration de la base de données** — Modifiez les identifiants dans `MyConnection.java` :

```java
private String url  = "jdbc:mysql://localhost:3306/firma";
private String user = "root";
private String password = "";
```

> `DB_connection` (Marketplace) délègue automatiquement à `MyConnection` — une seule connexion partagée.

---

## 🗄 Base de données

```bash
# Créer la base
mysql -u root -p -e "CREATE DATABASE firma;"

# Importer le schéma
mysql -u root -p firma < src/main/java/Firma/database/firma.sql

# Appliquer les migrations
mysql -u root -p firma < migration_code_participation.sql
mysql -u root -p firma < src/main/java/Firma/database/migration_accompagnants.sql
mysql -u root -p firma < src/main/java/Firma/database/migration_integration.sql
```

<details>
<summary><b>Tables principales</b></summary>

| Module | Table | Description |
|--------|-------|-------------|
| **Événements** | `evenements` | Événements avec capacité, lieu, dates, statut |
| | `participation` | Inscriptions des utilisateurs aux événements |
| | `accompagnant` | Accompagnants rattachés aux participations |
| **Marketplace** | `equipements` | Équipements agricoles en vente |
| | `vehicules` | Véhicules disponibles à la location |
| | `terrains` | Terrains agricoles disponibles à la location |
| | `categories` | Catégories (équipements, véhicules, terrains) |
| | `fournisseurs` | Fournisseurs d'équipements |
| | `achats_fournisseurs` | Achats auprès des fournisseurs |
| | `commandes` | Commandes clients (paiement + livraison) |
| | `details_commandes` | Lignes de commande (équipement, quantité, prix) |
| | `locations` | Locations de véhicules et terrains |
| **Commun** | `utilisateur` | Comptes utilisateurs (admin / client) |

</details>

---

## 🚀 Lancement

```bash
mvn clean javafx:run
```

Ou lancez directement la classe `Firma.test.MainFX` depuis votre IDE.

### Flux de navigation

```
🔐 Login
 │
 ├─── 👔 Admin Dashboard
 │     ├── Accueil ─── KPIs · Charts · Raccourcis
 │     ├── Marketplace ─── Équipements · Véhicules · Terrains (CRUD)
 │     ├── Fournisseurs ─── CRUD · Suivi achats
 │     ├── Commandes & Locations ─── Suivi · Statuts
 │     ├── Événements ─── Dashboard analytique · CRUD
 │     ├── Forum ─── Modération · Gestion discussions
 │     ├── Technicien ─── Planification · Maintenance
 │     └── Déconnexion
 │
 └─── 🧑‍🌾 Client Dashboard
       ├── Accueil ─── Bienvenue · Raccourcis services
       ├── Profil ─── Mon compte · Paramètres
       ├── Marketplace ─── Catalogue · Détail · Panier · Stripe
       ├── Mes Locations ─── Véhicules · Terrains loués
       ├── Événements ─── Parcourir · S'inscrire · Tickets
       ├── Forum ─── Discussions · Questions
       ├── Technicien ─── Demandes d'intervention
       └── Déconnexion
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
| **Tools** | `JavaFXPreservationTest` | Préservation JavaFX WebView |
| | `MapPickerBugConditionTest` | Conditions du MapPicker |
| | `SimpleWebViewTest` | WebView basiques |

> ⚠️ Les tests nécessitent une connexion active à la base de données MySQL locale.

---

## 📁 Structure du projet

<details>
<summary><b>Cliquez pour afficher l'arborescence complète</b></summary>

```
firma/
├── pom.xml                                     # Configuration Maven + dépendances
├── README.md                                   # Documentation du projet
├── migration_code_participation.sql            # Migration SQL
│
├── src/main/java/Firma/
│   ├── controllers/
│   │   ├── GestionEvenement/                   # 12 contrôleurs événements
│   │   └── GestionMarketplace/                 # 16 contrôleurs marketplace
│   │
│   ├── entities/
│   │   ├── GestionEvenement/                   # Evenement, Participation, Accompagnant…
│   │   └── GestionMarketplace/                 # Equipement, Vehicule, Terrain, Commande…
│   │
│   ├── services/
│   │   ├── GestionEvenement/                   # CRUD + statistiques événements
│   │   └── GestionMarketplace/                 # CRUD + Stripe + Email + PDF + alertes
│   │
│   ├── interfaces/                             # Interfaces CRUD génériques (IService)
│   │
│   ├── tools/
│   │   ├── GestionEvenement/                   # DB, Session, Email, QR, Météo, IA
│   │   └── GestionMarketplace/                 # DB adapter, MapPicker (Leaflet)
│   │
│   ├── database/                               # Schéma SQL + migrations
│   └── test/                                   # MainFX (point d'entrée)
│
├── src/main/resources/
│   ├── LoginApplication.fxml                   # Vue login
│   ├── GestionEvenement/                       # FXML + CSS événements
│   ├── GestionMarketplace/marketplace/GUI/     # FXML + CSS + Leaflet marketplace
│   └── image/                                  # Assets visuels
│
└── src/test/java/Firma/                        # Tests JUnit 5 + jqwik
```

</details>

---

## 📝 Fonctionnalités détaillées

### 🔐 Authentification & Utilisateurs
- Connexion par email / mot de passe avec validation regex
- Routage automatique par rôle : **Admin** → Dashboard Admin · **Client** → Dashboard Client
- Gestion de session via `SessionManager` (singleton)
- Profil utilisateur · déconnexion avec nettoyage de session

### 🏠 Tableaux de bord
- **Admin** — KPIs globaux (événements, participations, commandes, revenus) · PieChart · BarChart · top événements · raccourcis modules
- **Client** — Bienvenue personnalisée · raccourcis visuels (Marketplace, Événements, Forum, Technicien, Profil) · sections de présentation

---

### 🛒 Marketplace

| Fonctionnalité | Admin | Client |
|----------------|:-----:|:------:|
| Équipements — CRUD, stock, seuil d'alerte, images | ✅ | — |
| Véhicules — CRUD, tarifs jour/semaine/mois | ✅ | — |
| Terrains — CRUD, superficie, caution | ✅ | — |
| Fournisseurs — CRUD, suivi achats | ✅ | — |
| Commandes — Suivi paiement & livraison | ✅ | — |
| Locations — Suivi réservations | ✅ | ✅ |
| Statistiques — Revenus, catégories, stock | ✅ | — |
| Catalogue visuel + détail produit | — | ✅ |
| Panier + paiement Stripe | — | ✅ |
| Reçus PDF + email confirmation | — | ✅ |
| Carte Leaflet (sélection d'adresse) | — | ✅ |
| Alertes de stock par email | ✅ | — |

### 📅 Gestion d'Événements

| Fonctionnalité | Admin | Client |
|----------------|:-----:|:------:|
| CRUD événements (formulaires validés) | ✅ | — |
| Génération d'images IA (Hugging Face SDXL) | ✅ | — |
| Dashboard analytique (KPIs, charts) | ✅ | — |
| Liste participants + accompagnants | ✅ | — |
| Parcourir & s'inscrire aux événements | — | ✅ |
| Accompagnants dynamiques | — | ✅ |
| Tickets PDF + QR codes (`PART-XXXXX`) | — | ✅ |
| Confirmation email + ticket PDF en PJ | — | ✅ |
| Météo intégrée (Open-Meteo) | — | ✅ |
| Google Maps (localisation événement) | — | ✅ |
| Serveur HTTP embarqué (scan QR → ticket mobile) | ✅ | ✅ |

### 💬 Forum
- Espace de discussion communautaire pour agriculteurs
- Questions / Réponses · conseils techniques · retours d'expérience
- Modération par l'administrateur

### 🔧 Technicien
- Planification et suivi des interventions terrain
- Historique de maintenance · attribution de missions

### 📄 Documents & Emails
- **Tickets PDF** — Code unique + QR par personne · design brandé FIRMA
- **Reçus PDF** — Récapitulatif commande · tableau d'articles · branding
- **Emails** — Confirmation événement (lien cliquable) · ticket PDF en PJ · reçu paiement · alertes stock
- Envoi asynchrone via Jakarta Mail (SMTP Gmail)

---

## 👥 Auteurs

| Membre | Module |
|--------|--------|
| **Naama** | Gestion d'Événements |
| **Hamza** | Marketplace |
| *Équipe* | Forum · Technicien · Utilisateurs |

> Projet académique — **ESPRIT** (3A7) — Usage éducatif uniquement.

---

<p align="center">
  <b>FIRMA</b> — Gestion agricole intelligente 🌾
</p>
