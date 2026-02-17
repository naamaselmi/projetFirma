# 🎫 Firma — Gestion d'Événements et Participations

Application de bureau JavaFX pour la gestion complète d'événements : création, inscription, suivi des participants, accompagnants, et génération de tickets PDF.

---

## 📋 Table des matières

- [Aperçu](#aperçu)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation et Configuration](#installation-et-configuration)
- [Base de données](#base-de-données)
- [Lancement](#lancement)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [Auteurs](#auteurs)

---

## Aperçu

**Firma** est une application Java/JavaFX de gestion événementielle développée dans le cadre d'un projet académique (ESPRIT 3A7). Elle permet à un administrateur de créer et gérer des événements, et aux visiteurs de s'inscrire, gérer leurs participations, ajouter des accompagnants et exporter leurs tickets en PDF.

---

## Technologies

| Composant         | Technologie                |
|-------------------|----------------------------|
| Langage           | Java 17                    |
| Interface         | JavaFX 20.0.2 + FXML + CSS|
| Base de données   | MySQL 8.x / MariaDB 10.4  |
| Accès DB          | JDBC (MySQL Connector 8.0.30) |
| Build             | Maven                      |
| PDF               | iText 7.2.5                |
| Tests             | JUnit 5.10.2               |
| Logging           | SLF4J 2.0.9 + Logback      |

---

## Architecture

Le projet suit le pattern **MVC** (Modèle-Vue-Contrôleur) avec un système de **délégation** dans les contrôleurs :

```
┌─────────────────────────────────────────────────┐
│                    FXML (Vue)                    │
│  LoginApplication.fxml │ Dashboard.fxml │ front.fxml │
├─────────────────────────────────────────────────┤
│              Contrôleurs principaux              │
│  LoginController │ EvenementController │ FrontController │
├─────────────────────────────────────────────────┤
│             Contrôleurs délégués                 │
│  FormulaireCreationModificationEvenement         │
│  ConstructionCartesEvenement                     │
│  ConstructionCartesVisiteur                      │
│  GestionParticipationsVisiteur                   │
│  AffichageListeParticipants                      │
│  AffichageTicketsEtExportPDF                     │
│  OutilsInterfaceGraphique                        │
├─────────────────────────────────────────────────┤
│                  Services (DAO)                  │
│  EvenementService │ ParticipationService         │
│  AccompagnantService │ UtilisateurService         │
├─────────────────────────────────────────────────┤
│                   Entités                        │
│  Evenement │ Participation │ Utilisateur          │
│  Accompagnant │ Enums (Type, Statut, Role...)    │
├─────────────────────────────────────────────────┤
│              Outils / Connexion                  │
│  MyConnection (Singleton) │ SessionManager        │
└─────────────────────────────────────────────────┘
```

---

## Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.8+**
- **MySQL 8.x** ou **MariaDB 10.4+**
- Un IDE Java (IntelliJ IDEA, Eclipse, VS Code…)

---

## Installation et Configuration

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/<votre-username>/firma.git
   cd firma
   ```

2. **Configurer la base de données** :
   Modifiez les paramètres de connexion dans `src/main/java/edu/connection3a7/tools/MyConnection.java` si nécessaire :
   ```java
   private final String URL = "jdbc:mysql://localhost:3306/firma";
   private final String USER = "root";
   private final String PASSWORD = "";
   ```

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
   mysql -u root -p firma < src/main/java/edu/connection3a7/database/firma.sql
   ```

3. **Appliquer la migration** (colonne `code_participation`) :
   ```bash
   mysql -u root -p firma < migration_code_participation.sql
   ```

### Tables principales

| Table             | Description                                  |
|-------------------|----------------------------------------------|
| `evenement`       | Événements avec capacité, lieu, dates, statut|
| `participation`   | Inscriptions des utilisateurs aux événements |
| `accompagnant`    | Accompagnants rattachés aux participations   |
| `utilisateur`     | Comptes utilisateurs (admin / utilisateur)   |

---

## Lancement

```bash
mvn clean javafx:run
```

Ou depuis votre IDE, exécutez la classe principale :
```
edu.connection3a7.test.MainFX
```

### Comptes de test

| Rôle          | Email                | Mot de passe |
|---------------|----------------------|--------------|
| Administrateur| *(à insérer en BDD)* | *(à définir)*|
| Utilisateur   | *(à insérer en BDD)* | *(à définir)*|

---

## Tests

Le projet inclut des tests unitaires JUnit 5 pour les services CRUD :

```bash
mvn test
```

### Classes de test

| Classe                        | Couverture                                    |
|-------------------------------|-----------------------------------------------|
| `EvenementServiceTest`        | CRUD complet + mise à jour statut             |
| `ParticipationServiceTest`    | CRUD + accompagnants + code participation     |
| `AccompagnantServiceTest`     | CRUD + batch + réattribution                  |

> ⚠️ Les tests nécessitent une connexion active à la base de données MySQL locale.

---

## Structure du projet

```
firma/
├── pom.xml                          # Configuration Maven
├── README.md                        # Ce fichier
├── INTEGRATION_PARTICIPATIONS.md    # Documentation technique participations
├── migration_code_participation.sql # Migration SQL
│
├── src/main/java/edu/connection3a7/
│   ├── controllers/                 # Contrôleurs JavaFX
│   │   ├── LoginController.java            # Authentification
│   │   ├── EvenementController.java        # Dashboard admin
│   │   ├── FrontController.java            # Interface visiteur
│   │   ├── FormulaireCreationModificationEvenement.java
│   │   ├── ConstructionCartesEvenement.java
│   │   ├── ConstructionCartesVisiteur.java
│   │   ├── GestionParticipationsVisiteur.java
│   │   ├── AffichageListeParticipants.java
│   │   ├── AffichageTicketsEtExportPDF.java
│   │   └── OutilsInterfaceGraphique.java   # Utilitaires UI partagés
│   │
│   ├── entities/                    # Modèles de données
│   │   ├── Evenement.java
│   │   ├── Participation.java
│   │   ├── Utilisateur.java
│   │   ├── Accompagnant.java
│   │   └── Enums: Type, Statut, Statutevent, Role
│   │
│   ├── services/                    # Couche d'accès aux données (DAO)
│   │   ├── EvenementService.java
│   │   ├── ParticipationService.java
│   │   ├── AccompagnantService.java
│   │   └── UtilisateurService.java
│   │
│   ├── interfaces/
│   │   └── IService.java            # Interface générique CRUD
│   │
│   ├── tools/
│   │   ├── MyConnection.java        # Connexion DB Singleton
│   │   └── SessionManager.java      # Gestion de session utilisateur
│   │
│   ├── database/
│   │   ├── firma.sql                # Schéma complet de la BDD
│   │   └── migration_accompagnants.sql
│   │
│   └── test/
│       ├── MainFX.java              # Point d'entrée JavaFX
│       └── Main.java                # Tests console
│
├── src/main/resources/
│   ├── LoginApplication.fxml        # Vue login
│   ├── Dashboard.fxml               # Vue admin (événements)
│   ├── front.fxml                   # Vue visiteur
│   ├── evenement-style.css          # Styles dashboard
│   ├── front-style.css              # Styles front visiteur
│   └── image/                       # Ressources graphiques
│
└── src/test/java/edu/connection3a7/services/
    ├── EvenementServiceTest.java
    ├── ParticipationServiceTest.java
    └── AccompagnantServiceTest.java
```

---

## Fonctionnalités

### 🔐 Authentification
- Connexion par email/mot de passe avec validation regex
- Routage automatique selon le rôle (admin → Dashboard, utilisateur → Front)
- Gestion de session via `SessionManager`

### 📅 Gestion des Événements (Admin)
- Créer, modifier, supprimer, annuler des événements
- Validation complète des formulaires (titre, dates, horaires, capacité)
- Upload d'image pour chaque événement
- Recherche par titre et tri (date, titre, capacité, lieu)
- Affichage des participants avec statistiques

### 🎟️ Participations (Visiteur)
- Parcourir les événements avec cartes visuelles
- S'inscrire à un événement avec accompagnants dynamiques
- Modifier ou annuler sa participation
- Consulter ses participations et tickets
- Jauge visuelle des places disponibles

### 📄 Tickets et Export PDF
- Génération de tickets avec code unique (format `PART-XXXXX`)
- Tickets individuels pour chaque accompagnant (`PART-XXXXX-A1`, `-A2`…)
- Export PDF complet via iText 7 (tableaux formatés, badges colorés)

### 🗺️ Intégration Google Maps
- Ouverture de l'adresse de l'événement dans Google Maps via le navigateur

---

## Auteurs

- **Hamza** — Développeur principal
- Projet académique — **ESPRIT** (3A7)

---

## Licence

Projet académique — usage éducatif uniquement.
