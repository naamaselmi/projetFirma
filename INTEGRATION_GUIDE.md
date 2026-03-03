# 🔧 FIRMA — Guide d'Intégration pour les Nouveaux Modules

## Pour : Gestion Technicien · Gestion Forum · Gestion Utilisateur

> Ce document est destiné aux membres de l'équipe qui intègrent les modules **Technicien**, **Forum** et **Utilisateur** dans l'application FIRMA existante. Lisez-le ENTIÈREMENT avant de coder.

---

## 📋 Table des matières

1. [Vue d'ensemble de l'existant](#1-vue-densemble-de-lexistant)
2. [Architecture — Ce que vous devez respecter](#2-architecture--ce-que-vous-devez-respecter)
3. [Où placer vos fichiers](#3-où-placer-vos-fichiers)
4. [Comment se connecter à la base de données](#4-comment-se-connecter-à-la-base-de-données)
5. [Comment récupérer l'utilisateur connecté](#5-comment-récupérer-lutilisateur-connecté)
6. [Comment s'intégrer dans le Dashboard (Navigation)](#6-comment-sintégrer-dans-le-dashboard-navigation)
7. [Règles impératives — À FAIRE](#7-règles-impératives--à-faire)
8. [Règles impératives — À NE PAS FAIRE](#8-règles-impératives--à-ne-pas-faire)
9. [Feuille de route par module](#9-feuille-de-route-par-module)
10. [Checklist avant de push](#10-checklist-avant-de-push)
11. [Référence rapide — Exemples de code](#11-référence-rapide--exemples-de-code)

---

## 1. Vue d'ensemble de l'existant

L'application FIRMA possède déjà **deux modules fonctionnels** :

| Module | Description | Status |
|--------|-------------|--------|
| **GestionMarketplace** | Équipements, véhicules, terrains, commandes, paiements, locations | ✅ Complet |
| **GestionEvenement** | Événements, participations, accompagnants, tickets PDF, QR codes | ✅ Complet |
| **GestionTechnicien** | Gestion des techniciens | 🔲 À intégrer |
| **GestionForum** | Forum de discussion | 🔲 À intégrer |
| **GestionUtilisateur** | Profil et gestion des utilisateurs | 🔲 À intégrer |

### Navigation actuelle

```
LoginApplication.fxml (fenêtre 742×480)
  │
  ├─ Admin → AdminDashboard.fxml (plein écran)
  │    Sidebar : Accueil | Marketplace | Événements | Technicien* | Forum* | Utilisateur*
  │    (* = affichent un placeholder "This is gestion X")
  │
  └─ Client → client_dashboard.fxml (plein écran)
       Sidebar : Accueil | Marketplace | Événements | Technicien* | Forum* | Profil*
       (* = affichent un placeholder)
```

**Les boutons sidebar existent déjà.** Vous devez simplement créer les FXML + Controllers et les brancher.

---

## 2. Architecture — Ce que vous devez respecter

Le projet suit le pattern **MVC** avec la structure de packages suivante :

```
Firma/
├── entities/GestionXxx/           ← Vos entités (POJO Java)
├── interfaces/GestionXxx/         ← Votre IService<T> (optionnel, copier celui existant)
├── services/GestionXxx/           ← Vos services (DAO — JDBC direct)
├── controllers/GestionXxx/        ← Vos contrôleurs JavaFX
├── tools/GestionXxx/              ← Vos utilitaires (si besoin)
└── database/                      ← Vos scripts SQL (migrations)
```

Ressources FXML :
```
src/main/resources/
├── GestionTechnicien/             ← Vos FXML + CSS
├── GestionForum/                  ← Vos FXML + CSS
└── GestionUtilisateur/            ← Vos FXML + CSS
```

---

## 3. Où placer vos fichiers

### Exemple pour le module Forum :

| Type | Chemin |
|------|--------|
| **Entités** | `src/main/java/Firma/entities/GestionForum/Post.java` |
| **Entités** | `src/main/java/Firma/entities/GestionForum/Commentaire.java` |
| **Services** | `src/main/java/Firma/services/GestionForum/PostService.java` |
| **Services** | `src/main/java/Firma/services/GestionForum/CommentaireService.java` |
| **Contrôleurs** | `src/main/java/Firma/controllers/GestionForum/ForumController.java` |
| **Interfaces** | `src/main/java/Firma/interfaces/GestionForum/IService.java` |
| **FXML (admin)** | `src/main/resources/GestionForum/AdminForumContent.fxml` |
| **FXML (client)** | `src/main/resources/GestionForum/ClientForumContent.fxml` |
| **CSS** | `src/main/resources/GestionForum/forum-style.css` |
| **SQL** | `src/main/java/Firma/database/migration_forum.sql` |

> ⚠️ Même logique pour `GestionTechnicien/` et `GestionUtilisateur/`.

---

## 4. Comment se connecter à la base de données

### ❌ NE CRÉEZ PAS une nouvelle connexion DB.

Il existe **une seule connexion** partagée par tous les modules :

```java
// ✅ CORRECT — Dans vos services, faites ceci :
import Firma.tools.GestionEvenement.MyConnection;

public class PostService {
    private Connection cnx = MyConnection.getInstance().getCnx();
    
    public List<Post> getAll() throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY date_creation DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        // ...
    }
}
```

```java
// ❌ INTERDIT — Ne faites JAMAIS ceci :
Connection cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/firma", "root", "");
```

**Pourquoi ?** `MyConnection` est un singleton. Si vous créez une 2ème connexion, vous risquez des locks MySQL, des données incohérentes, et des fuites de connexion.

> **Alternative (optionnelle)** : Vous pouvez aussi utiliser `DB_connection` de GestionMarketplace, qui délègue à `MyConnection` :
> ```java
> import Firma.tools.GestionMarketplace.DB_connection;
> private Connection cnx = DB_connection.getInstance().getConnection();
> ```
> C'est exactement la même connexion.

---

## 5. Comment récupérer l'utilisateur connecté

L'utilisateur connecté est stocké dans un **singleton** `SessionManager` :

```java
import Firma.tools.GestionEvenement.SessionManager;
import Firma.entities.GestionEvenement.Utilisateur;

// Récupérer l'utilisateur courant
Utilisateur user = SessionManager.getInstance().getUtilisateur();

// Infos disponibles :
user.getId()           // int — ID en base
user.getNom()          // String — Nom de famille
user.getPrenom()       // String — Prénom
user.getEmail()        // String — Email
user.getTypeUser()     // Role — Role.admin ou Role.client
user.getTelephone()    // String
user.getAdresse()      // String
user.getVille()        // String
user.getDateCreation() // LocalDateTime
```

### Important :
- **NE modifiez PAS** le `SessionManager` existant.
- **NE créez PAS** votre propre système de session.
- `SessionManager.getInstance().getUtilisateur()` est **toujours non-null** quand votre vue est affichée (l'utilisateur est forcément connecté pour voir le dashboard).
- L'entité `Utilisateur` est dans `Firma.entities.GestionEvenement` — importez-la de là.

---

## 6. Comment s'intégrer dans le Dashboard (Navigation)

### 🎯 C'est la partie la plus critique.

Les dashboards admin et client ont une **sidebar** avec des boutons déjà câblés vers des méthodes `handleXxx()`. Actuellement, vos modules affichent un placeholder :

```java
// AdminDashboardController.java — ÉTAT ACTUEL
@FXML
void handleForum(ActionEvent event) {
    setActiveButton(btnForum);
    loadPlaceholder("Forum");  // ← Vous devez remplacer ça
}

@FXML
void handleTechnicien(ActionEvent event) {
    setActiveButton(btnTechnicien);
    loadPlaceholder("Technicien");  // ← Vous devez remplacer ça
}

@FXML
void handleUtilisateur(ActionEvent event) {
    setActiveButton(btnUtilisateur);
    loadPlaceholder("Utilisateur");  // ← Vous devez remplacer ça
}
```

### Ce que vous devez faire :

**Étape 1** — Créer votre FXML comme un contenu qui sera chargé dans un `StackPane` (PAS une fenêtre complète). Votre FXML doit être une vue partielle (pas de sidebar, pas de menu) :

```xml
<!-- GestionForum/AdminForumContent.fxml -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.Label?>

<VBox xmlns="http://javafx.com/javafx/17"
      xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="Firma.controllers.GestionForum.AdminForumController"
      spacing="10" style="-fx-padding: 20; -fx-background-color: #fefbde;">
    
    <!-- VOTRE CONTENU ICI -->
    <Label text="Gestion Forum" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
    
</VBox>
```

**Étape 2** — Remplacer le placeholder dans `AdminDashboardController.java` :

```java
// AVANT (placeholder) :
@FXML
void handleForum(ActionEvent event) {
    setActiveButton(btnForum);
    loadPlaceholder("Forum");
}

// APRÈS (votre vue) :
@FXML
void handleForum(ActionEvent event) {
    setActiveButton(btnForum);
    loadView("/GestionForum/AdminForumContent.fxml");
}
```

**Étape 3** — Faire la même chose dans `ClientDashboardController.java` :

```java
// AVANT :
@FXML
void handleForum(ActionEvent event) {
    setActiveButton(btnForum, lblForum, iconForum);
    loadPlaceholder("Forum");
}

// APRÈS :
@FXML
void handleForum(ActionEvent event) {
    setActiveButton(btnForum, lblForum, iconForum);
    loadView("/GestionForum/ClientForumContent.fxml");
}
```

### ⚠️ Attention — La méthode `loadView()` existe déjà !

```java
// ADMIN — loadView prend un chemin FXML et le charge dans contentArea :
private void loadView(String fxmlPath) {
    Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
    contentArea.getChildren().setAll(view);
}
```

```java
// CLIENT — même chose :
private void loadView(String fxmlPath) {
    Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
    contentArea.getChildren().setAll(view);
}
```

Vous n'avez **rien d'autre à faire** côté navigation. Le bouton sidebar, le style actif, tout est déjà géré.

### Boutons sidebar — fx:id de référence :

| Bouton | fx:id Admin | fx:id Client | Handler Admin | Handler Client |
|--------|-------------|--------------|---------------|----------------|
| Technicien | `btnTechnicien` | `btnTechnicien` | `handleTechnicien` | `handleTechnicien` |
| Forum | `btnForum` | `btnForum` | `handleForum` | `handleForum` |
| Utilisateur | `btnUtilisateur` | `btnProfil` | `handleUtilisateur` | `handleProfil` |

> **Note** : Côté client, le bouton Utilisateur s'appelle "Profil" (`btnProfil` / `handleProfil`). C'est normal — le client gère son profil, l'admin gère tous les utilisateurs.

---

## 7. Règles impératives — ✅ À FAIRE

### Structure
- ✅ **Créer vos packages** sous `Firma/entities/GestionXxx/`, `Firma/services/GestionXxx/`, `Firma/controllers/GestionXxx/`
- ✅ **Créer vos FXML** sous `src/main/resources/GestionXxx/`
- ✅ **Écrire un fichier SQL de migration** pour vos nouvelles tables → `Firma/database/migration_xxx.sql`
- ✅ **Implémenter l'interface `IService<T>`** dans vos services (soit celle de GestionEvenement soit celle de GestionMarketplace)

### Base de données
- ✅ **Utiliser `MyConnection.getInstance().getCnx()`** pour la connexion DB
- ✅ **Utiliser des `PreparedStatement`** (jamais de concaténation SQL)
- ✅ **Fermer les `ResultSet` et `PreparedStatement`** après utilisation (try-with-resources)
- ✅ **Préfixer vos tables** de façon claire : `techniciens`, `interventions`, `posts`, `commentaires`, etc.
- ✅ **Utiliser la table `utilisateur` existante** pour les FK vers les utilisateurs

### Session
- ✅ **Utiliser `SessionManager.getInstance().getUtilisateur()`** pour accéder au user courant
- ✅ **Vérifier le rôle** si votre vue doit afficher des choses différentes selon admin/client :
  ```java
  if (SessionManager.getInstance().getUtilisateur().getTypeUser() == Role.admin) {
      // logique admin
  }
  ```

### UI / FXML
- ✅ **Votre FXML = une vue partielle** (pas de fenêtre, pas de sidebar — juste le contenu)
- ✅ **Respecter la palette de couleurs** : fond `#fefbde` (crème), accent `#49ad32` (vert FIRMA)
- ✅ **Tester avec `mvn compile`** avant de push — le build doit rester **BUILD SUCCESS**
- ✅ **Créer un FXML admin ET un FXML client** si votre module a les deux vues

### Git
- ✅ **Travailler sur votre propre branche** : `feature/gestion-technicien`, `feature/gestion-forum`, `feature/gestion-utilisateur`
- ✅ **Ne modifier que 2 lignes** dans les DashboardControllers (le `loadPlaceholder` → `loadView`)
- ✅ **Écrire au moins un test unitaire** pour vos services

---

## 8. Règles impératives — ❌ À NE PAS FAIRE

### Interdictions absolues

| ❌ NE PAS | Pourquoi |
|-----------|----------|
| Créer une nouvelle `Connection` / `DriverManager.getConnection()` | Utilisez `MyConnection` — une seule connexion partagée |
| Créer votre propre système de session / `static Utilisateur currentUser` | Utilisez `SessionManager` — c'est le singleton officiel |
| Modifier `MyConnection.java` | Partagé par tous les modules, toute modification casse tout |
| Modifier `SessionManager.java` | Partagé par tous les modules |
| Modifier `LoginController.java` | Le login fonctionne déjà — ne le touchez pas |
| Modifier les FXML de dashboard (`AdminDashboard.fxml`, `client_dashboard.fxml`) | La sidebar est déjà configurée — pas besoin de toucher |
| Ouvrir une nouvelle fenêtre / `Stage` pour votre module | Votre vue est chargée DANS le `contentArea` du dashboard |
| Utiliser `stage.setScene(new Scene(...))` dans votre module | C'est réservé à la déconnexion / login uniquement |
| Supprimer / renommer des fichiers existants | Vous ne touchez qu'à VOS fichiers + 2 lignes de handleXxx |
| Modifier les entités/services d'autres modules (Marketplace, Evenement) | Chaque module est indépendant |
| Utiliser des `fx:id` identiques à ceux du dashboard (`btnAccueil`, `contentArea`, etc.) | Conflit de noms dans le FXML loader |
| Hardcoder des chemins Windows (`C:\Users\...`) | Utilisez des chemins relatifs et `getClass().getResource(...)` |
| Ajouter des dépendances Maven sans consulter l'équipe | Le `pom.xml` est partagé |

### Pièges courants

- **Ne pas créer une `class Utilisateur` dans votre package** → Utilisez `Firma.entities.GestionEvenement.Utilisateur`
- **Ne pas créer un `enum Role`** → Utilisez `Firma.entities.GestionEvenement.Role`
- **Ne pas faire `new Stage()`** → Votre contenu est injecté dans `contentArea` (un `StackPane`)
- **Ne pas ajouter de sidebar dans votre FXML** → La sidebar est déjà dans le dashboard parent

---

## 9. Feuille de route par module

### 🔧 Gestion Technicien

**Tables suggérées :**
```sql
CREATE TABLE techniciens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT,               -- FK vers utilisateur.id
    specialite VARCHAR(100),
    disponible BOOLEAN DEFAULT TRUE,
    note_moyenne DECIMAL(3,2),
    date_creation DATETIME DEFAULT NOW(),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE TABLE interventions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    technicien_id INT,                 -- FK vers techniciens.id
    client_id INT,                     -- FK vers utilisateur.id
    description TEXT,
    statut ENUM('EN_ATTENTE','EN_COURS','TERMINEE','ANNULEE'),
    date_demande DATETIME DEFAULT NOW(),
    date_intervention DATETIME,
    commentaire TEXT,
    note INT,                          -- notation 1-5
    FOREIGN KEY (technicien_id) REFERENCES techniciens(id),
    FOREIGN KEY (client_id) REFERENCES utilisateur(id)
);
```

**Fichiers à créer :**
```
entities/GestionTechnicien/     → Technicien.java, Intervention.java
services/GestionTechnicien/     → TechnicienService.java, InterventionService.java
controllers/GestionTechnicien/  → AdminTechnicienController.java, ClientTechnicienController.java
resources/GestionTechnicien/    → AdminTechnicienContent.fxml, ClientTechnicienContent.fxml, technicien-style.css
```

**Fonctionnalités attendues :**
- **Admin** : CRUD techniciens, voir/gérer les interventions, statistiques
- **Client** : Demander une intervention, voir historique, noter un technicien

**Branchement dashboard :**
```java
// AdminDashboardController → handleTechnicien :
loadView("/GestionTechnicien/AdminTechnicienContent.fxml");

// ClientDashboardController → handleTechnicien :
loadView("/GestionTechnicien/ClientTechnicienContent.fxml");
```

---

### 💬 Gestion Forum

**Tables suggérées :**
```sql
CREATE TABLE forum_posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    utilisateur_id INT,                -- FK vers utilisateur.id
    titre VARCHAR(200) NOT NULL,
    contenu TEXT NOT NULL,
    categorie VARCHAR(50),
    date_creation DATETIME DEFAULT NOW(),
    date_modification DATETIME,
    likes_count INT DEFAULT 0,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE TABLE forum_commentaires (
    id INT PRIMARY KEY AUTO_INCREMENT,
    post_id INT,                       -- FK vers forum_posts.id
    utilisateur_id INT,                -- FK vers utilisateur.id
    contenu TEXT NOT NULL,
    date_creation DATETIME DEFAULT NOW(),
    FOREIGN KEY (post_id) REFERENCES forum_posts(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);
```

**Fichiers à créer :**
```
entities/GestionForum/          → Post.java, Commentaire.java
services/GestionForum/          → PostService.java, CommentaireService.java
controllers/GestionForum/       → AdminForumController.java, ClientForumController.java
resources/GestionForum/         → AdminForumContent.fxml, ClientForumContent.fxml, forum-style.css
```

**Fonctionnalités attendues :**
- **Admin** : Modérer les posts, supprimer des commentaires, voir les statistiques
- **Client** : Créer/modifier/supprimer ses posts, commenter, liker

**Branchement dashboard :**
```java
// AdminDashboardController → handleForum :
loadView("/GestionForum/AdminForumContent.fxml");

// ClientDashboardController → handleForum :
loadView("/GestionForum/ClientForumContent.fxml");
```

---

### 👤 Gestion Utilisateur / Profil

**Table existante :** `utilisateur` (déjà utilisée par le login)

> ⚠️ Vous ne créez PAS une nouvelle table utilisateur. Vous travaillez avec la table `utilisateur` existante !

**Fichiers à créer :**
```
controllers/GestionUtilisateur/ → AdminUtilisateurController.java, ProfilController.java
services/GestionUtilisateur/    → ProfilService.java (ou réutiliser UtilisateurService existant)
resources/GestionUtilisateur/   → AdminUtilisateurContent.fxml, ProfilContent.fxml, profil-style.css
```

**Fonctionnalités attendues :**
- **Admin** (`handleUtilisateur`) : Lister tous les utilisateurs, activer/désactiver des comptes, voir les activités, CRUD utilisateurs
- **Client** (`handleProfil`) : Voir et modifier son profil (nom, prénom, email, téléphone, adresse, ville), changer son mot de passe

**Branchement dashboard :**
```java
// AdminDashboardController → handleUtilisateur :
loadView("/GestionUtilisateur/AdminUtilisateurContent.fxml");

// ClientDashboardController → handleProfil :
loadView("/GestionUtilisateur/ProfilContent.fxml");
```

**Important :** Pour la modification du profil côté client, après mise à jour en base, rafraîchissez aussi le `SessionManager` :
```java
// Après UPDATE en base :
Utilisateur updated = profilService.getById(user.getId());
SessionManager.getInstance().setUtilisateur(updated);
```

---

## 10. Checklist avant de push

Avant chaque merge request, vérifiez :

- [ ] `mvn compile` → **BUILD SUCCESS** (0 erreurs)
- [ ] Mes fichiers sont dans les bons packages (`Firma/entities/GestionXxx/`, etc.)
- [ ] J'utilise `MyConnection.getInstance().getCnx()` pour la DB
- [ ] J'utilise `SessionManager.getInstance().getUtilisateur()` pour le user
- [ ] Mon FXML est une **vue partielle** (pas de sidebar, pas de stage)
- [ ] J'ai modifié **seulement** `loadPlaceholder(...)` → `loadView(...)` dans les dashboard controllers
- [ ] Je n'ai PAS modifié les fichiers FXML des dashboards
- [ ] Je n'ai PAS créé de nouvelle `Utilisateur` entity / `Role` enum / connexion DB
- [ ] Mon fichier SQL de migration est inclus dans `Firma/database/`
- [ ] Palette couleurs respectée : `#fefbde` (fond), `#49ad32` (accent vert)
- [ ] J'ai écrit au moins **1 test unitaire** pour mes services
- [ ] Je n'ai ajouté aucune dépendance Maven sans accord de l'équipe

---

## 11. Référence rapide — Exemples de code

### Service type (copier-coller et adapter) :

```java
package Firma.services.GestionForum;

import Firma.entities.GestionForum.Post;
import Firma.tools.GestionEvenement.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService {
    private Connection cnx = MyConnection.getInstance().getCnx();

    public void addEntity(Post post) throws SQLException {
        String sql = "INSERT INTO forum_posts (utilisateur_id, titre, contenu, categorie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, post.getUtilisateurId());
            ps.setString(2, post.getTitre());
            ps.setString(3, post.getContenu());
            ps.setString(4, post.getCategorie());
            ps.executeUpdate();
        }
    }

    public List<Post> getAll() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM forum_posts ORDER BY date_creation DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setUtilisateurId(rs.getInt("utilisateur_id"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setCategorie(rs.getString("categorie"));
                p.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                posts.add(p);
            }
        }
        return posts;
    }

    public void deleteEntity(int id) throws SQLException {
        String sql = "DELETE FROM forum_posts WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
```

### Controller type (copier-coller et adapter) :

```java
package Firma.controllers.GestionForum;

import Firma.entities.GestionForum.Post;
import Firma.services.GestionForum.PostService;
import Firma.tools.GestionEvenement.SessionManager;
import Firma.entities.GestionEvenement.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientForumController implements Initializable {

    @FXML private VBox postsContainer;
    @FXML private TextField searchField;

    private PostService postService = new PostService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // L'utilisateur est TOUJOURS disponible ici
        Utilisateur user = SessionManager.getInstance().getUtilisateur();
        System.out.println("Forum chargé pour : " + user.getPrenom() + " " + user.getNom());
        
        loadPosts();
    }

    private void loadPosts() {
        try {
            var posts = postService.getAll();
            postsContainer.getChildren().clear();
            for (Post p : posts) {
                // Construire les cartes de posts...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### FXML type (vue partielle) :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>

<!-- ⚠️ PAS de sidebar, PAS de MenuBar — juste le contenu -->
<ScrollPane fitToWidth="true"
            xmlns="http://javafx.com/javafx/17"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="Firma.controllers.GestionForum.ClientForumController"
            style="-fx-background: #fefbde;">
    
    <VBox spacing="15" style="-fx-padding: 25; -fx-background-color: #fefbde;">
        
        <!-- Header -->
        <HBox alignment="CENTER_LEFT" spacing="15">
            <Label text="💬 Forum" style="-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333;"/>
            <Region HBox.hgrow="ALWAYS"/>
            <TextField fx:id="searchField" promptText="Rechercher..." prefWidth="250"/>
            <Button text="Nouveau Post" style="-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-weight: bold;"/>
        </HBox>
        
        <!-- Posts container -->
        <VBox fx:id="postsContainer" spacing="10"/>
        
    </VBox>
</ScrollPane>
```

---

## Contacts

| Module | Responsable | Fichiers à modifier dans le dashboard |
|--------|-------------|---------------------------------------|
| Technicien | *à remplir* | `AdminDashboardController.handleTechnicien()` + `ClientDashboardController.handleTechnicien()` |
| Forum | *à remplir* | `AdminDashboardController.handleForum()` + `ClientDashboardController.handleForum()` |
| Utilisateur | *à remplir* | `AdminDashboardController.handleUtilisateur()` + `ClientDashboardController.handleProfil()` |

---

> 📅 Ce guide a été généré le 1 mars 2026 pour le projet FIRMA — ESPRIT 3A7.
