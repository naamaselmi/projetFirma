# 🏪 Marketplace Management System

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.14-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-green)
![Maven](https://img.shields.io/badge/Maven-3.x-red)

A comprehensive JavaFX desktop application for managing agricultural marketplace operations including equipment sales, vehicle rentals, terrain leasing, and order management. Built with modern Java architecture following MVC pattern.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Technologies](#-technologies)
- [Getting Started](#-getting-started)
- [Database Schema](#-database-schema)
- [Screenshots](#-screenshots)
- [API Reference](#-api-reference)
- [Testing](#-testing)
- [Contributing](#-contributing)

---

## ✨ Features

### 🛠️ Administration Features (Admin Dashboard)
- **🏪 Marketplace Hub** - Central management dashboard with 6 management modules
- **🛒 Equipment Management** - Full CRUD for agricultural equipment with stock alerts
- **🚗 Vehicle Management** - Fleet management with daily/weekly/monthly pricing
- **🌾 Terrain Management** - Agricultural land management with pricing
- **👥 Supplier Management** - Supplier relationships and purchase tracking
- **📍 Location/Rental Management** - Complete rental lifecycle management
  - View all rentals (vehicles & terrains)
  - Filter by type, status
  - Confirm, complete, or cancel rentals
  - Auto-update product availability
- **📦 Order Management** - Complete order tracking system
  - Payment status updates (En attente, Payé, Échoué)
  - Delivery status tracking (En attente → En préparation → Expédié → Livré)
  - Client information display
  - CSV export functionality

### 🛍️ Client Features (Client Dashboard)
- **🏠 Welcome Page** - Personalized client dashboard
- **🛒 Product Marketplace** - Browse available products
  - Filter by category
  - View product details
  - Add to cart functionality
- **🛒 Shopping Cart** - Cart management with checkout
- **💳 Payment System** - Integrated payment view
- **📋 Rental History** - View and manage personal rentals
  - View rental status
  - Delete terminated/cancelled rentals

### 👥 User Management
- **🔐 Role-Based Access** - Admin and Client user types
- **🖥️ Dual Dashboards** - Separate interfaces for administrators and clients
- **🔑 Secure Login** - Database-authenticated user sessions

---

## 🏗️ Architecture

The application follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │   Application   │  │          Controller             │   │
│  │  (JavaFX App)   │  │   (FXML Controllers)            │   │
│  └─────────────────┘  └─────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                     SERVICE LAYER                           │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  IService<T> Interface → Service Implementations       │ │
│  │  (CategorieService, EquipementService, etc.)           │ │
│  └────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      DATA LAYER                             │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │    Entities     │  │      DB_connection              │   │
│  │  (POJOs/Models) │  │   (Singleton Pattern)           │   │
│  └─────────────────┘  └─────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                       DATABASE                              │
│                    MySQL (gestion_mp)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
marketplace/
├── src/main/java/marketplace/
│   ├── Launcher.java                      # Application entry point
│   ├── GUI/
│   │   ├── Application/
│   │   │   └── LoginApplication.java      # JavaFX Application class
│   │   └── Controller/                    # 15 Controllers
│   │       ├── LoginController.java       # Authentication
│   │       ├── AdminDashboardController.java
│   │       ├── ClientDashboardController.java
│   │       ├── MarketplaceController.java # Admin marketplace hub
│   │       ├── ClientMarketplaceController.java # Client product browser
│   │       ├── EquipementController.java  # Equipment CRUD
│   │       ├── VehiculeController.java    # Vehicle CRUD
│   │       ├── TerrainController.java     # Terrain CRUD
│   │       ├── FournisseurController.java # Supplier CRUD
│   │       ├── LocationAdminController.java # Rental management
│   │       ├── CommandeAdminController.java # Order management
│   │       ├── CartPanelController.java   # Shopping cart
│   │       ├── PaymentController.java     # Payment processing
│   │       ├── ProductDetailController.java # Product details
│   │       └── RentalsPanelController.java # Client rentals
│   ├── entities/                          # 15 Domain models
│   │   ├── Categorie.java
│   │   ├── Fournisseur.java
│   │   ├── Equipement.java
│   │   ├── Vehicule.java
│   │   ├── Terrain.java
│   │   ├── Commande.java
│   │   ├── DetailCommande.java
│   │   ├── Location.java
│   │   ├── AchatFournisseur.java
│   │   ├── Utilisateur.java
│   │   ├── CartItem.java
│   │   ├── ProductType.java               # Enum
│   │   ├── PaymentStatus.java             # Enum
│   │   ├── DeliveryStatus.java            # Enum
│   │   └── RentalStatus.java              # Enum
│   ├── service/                           # 11 Services
│   │   ├── CategorieService.java
│   │   ├── FournisseurService.java
│   │   ├── EquipementService.java
│   │   ├── VehiculeService.java
│   │   ├── TerrainService.java
│   │   ├── CommandeService.java
│   │   ├── DetailCommandeService.java
│   │   ├── LocationService.java
│   │   ├── AchatFournisseurService.java
│   │   ├── UtilisateurService.java
│   │   └── CartService.java
│   ├── interfaces/
│   │   └── IService.java                  # Generic service interface
│   ├── tools/
│   │   └── DB_connection.java             # Database singleton
│   └── test/
│       └── TestMain.java                  # CRUD test suite
├── src/main/resources/
│   ├── marketplace/GUI/
│   │   ├── views/                         # 17 FXML files
│   │   │   ├── login.fxml
│   │   │   ├── AdminDashboard.fxml
│   │   │   ├── client_dashboard.fxml
│   │   │   ├── MarketplaceView.fxml       # Admin hub (6 cards)
│   │   │   ├── ClientMarketplaceView.fxml
│   │   │   ├── ClientAccueilView.fxml
│   │   │   ├── EquipementView.fxml
│   │   │   ├── VehiculeView.fxml
│   │   │   ├── TerrainView.fxml
│   │   │   ├── FournisseurView.fxml
│   │   │   ├── LocationAdminView.fxml     # Rental admin
│   │   │   ├── CommandeAdminView.fxml     # Order admin
│   │   │   ├── CartPanelView.fxml
│   │   │   ├── PaymentView.fxml
│   │   │   ├── ProductDetailView.fxml
│   │   │   └── RentalsPanelView.fxml
│   │   └── css/
│   │       └── styles.css                 # Application styles
│   └── image/                             # Application assets
│       ├── logo.png
│       ├── i1.png - i6.png                # Management icons
│       └── firma.png
└── pom.xml                                # Maven configuration
```

---

## 🛠️ Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Core language (LTS) |
| JavaFX | 17.0.14 | Desktop UI framework |
| MySQL | 8.0+ | Relational database |
| Maven | 3.x | Build & dependency management |
| JDBC | 8.0.33 | Database connectivity |
| JUnit | 5.12.1 | Unit testing |

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or higher
- **MySQL 8.0+** running locally
- **Maven 3.x** installed

### Database Setup

1. Create the database and user:
```sql
CREATE DATABASE gestion_mp;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON gestion_mp.* TO 'root'@'localhost';
```

2. Run the database schema (tables will be created automatically based on entity structure)

### Running the Application

```bash
# Clone the repository
cd gestion_marketplace/marketplace

# Compile and run
mvn clean compile
mvn javafx:run

# Or run directly from IDE
# Main class: marketplace.Launcher
```

### Running Tests

```bash
# Run CRUD tests for all services
mvn compile exec:java -Dexec.mainClass="marketplace.test.TestMain"
```

---

## 🗄️ Database Schema

### Database: `mp`

### Tables Overview

| Table | Description |
|-------|-------------|
| `utilisateurs` | Users (admin/client) with authentication |
| `categories` | Product categories (equipement, vehicule, terrain) |
| `equipements` | Agricultural equipment inventory |
| `vehicules` | Vehicle fleet for rental |
| `terrains` | Agricultural land for lease |
| `fournisseurs` | Supplier information |
| `commandes` | Customer orders |
| `details_commandes` | Order line items |
| `locations` | Rental records (vehicles & terrains) |
| `achats_fournisseurs` | Supplier purchases |

### Entity Relationship

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Categorie   │←────│  Equipement  │────→│  Fournisseur │
└──────────────┘     └──────────────┘     └──────────────┘
       ↑                    ↓                    ↓
       │             ┌──────────────┐     ┌──────────────┐
       │             │DetailCommande│     │AchatFourniss.│
       │             └──────────────┘     └──────────────┘
       │                    ↓
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Vehicule   │     │   Commande   │────→│ Utilisateur  │
└──────────────┘     └──────────────┘     └──────────────┘
       ↓                                         ↓
┌──────────────┐                          ┌──────────────┐
│   Location   │←─────────────────────────│   Terrain    │
└──────────────┘                          └──────────────┘
```

### Database Triggers

The database includes several triggers for automatic calculations:
- `before_location_insert` - Auto-calculates rental duration
- `before_commande_insert` - Auto-generates order numbers
- `after_commande_payee` - Updates stock on payment
- `before_detail_commande_insert` - Calculates subtotals

---

## 🖼️ Application Views

### Admin Dashboard
The admin dashboard provides access to 6 management modules through the Marketplace hub:

| Module | Description |
|--------|-------------|
| 🛠️ Équipements | Manage agricultural equipment inventory |
| 🌾 Terrains | Manage agricultural land listings |
| 🚗 Véhicules | Manage vehicle fleet |
| 👥 Fournisseurs | Manage supplier relationships |
| 📍 Locations | Monitor and manage all rentals |
| 📦 Commandes | Track and manage customer orders |

### Client Dashboard
The client interface includes:
- **Accueil** - Welcome page with user info
- **Marketplace** - Browse and filter available products
- **Panier** - Shopping cart management
- **Mes Locations** - Personal rental history

---

## 📚 API Reference

### IService<T> Interface

All services implement the generic `IService<T>` interface:

```java
public interface IService<T> {
    void addEntity(T entity) throws SQLException;
    void deleteEntity(T entity) throws SQLException;
    void updateEntity(T entity) throws SQLException;
    List<T> getEntities() throws SQLException;
}
```

### Service Methods

Each service extends the base interface with domain-specific methods:

| Service | Additional Methods |
|---------|-------------------|
| CategorieService | `getCategoriesByType(ProductType)` |
| EquipementService | `getAvailableEquipements()`, `getLowStockEquipements()`, `searchByName()` |
| VehiculeService | `getAvailableVehicules()`, `search()`, `updateDisponibilite()` |
| TerrainService | `getAvailableTerrains()`, `searchByVille()`, `updateDisponibilite()` |
| CommandeService | `getCommandesByUser()`, `getPendingCommandes()`, `updatePaymentStatus()`, `updateDeliveryStatus()` |
| LocationService | `getLocationsByUser()`, `updateStatus()`, `getActiveLocations()` |
| UtilisateurService | `authenticate()`, `getById()` |
| CartService | `addToCart()`, `removeFromCart()`, `getCartItems()`, `clearCart()` |

### Status Enums

**RentalStatus:**
- `EN_ATTENTE` - Waiting for confirmation
- `CONFIRMEE` - Confirmed
- `EN_COURS` - In progress
- `TERMINEE` - Completed
- `ANNULEE` - Cancelled

**PaymentStatus:**
- `EN_ATTENTE` - Pending
- `PAYE` - Paid
- `ECHOUE` - Failed
- `PARTIEL` - Partial

**DeliveryStatus:**
- `EN_ATTENTE` - Pending
- `EN_PREPARATION` - Preparing
- `EXPEDIE` - Shipped
- `LIVRE` - Delivered
- `ANNULE` - Cancelled

---

## 🧪 Testing

The project includes a comprehensive test suite in `TestMain.java`:

- ✅ Full CRUD tests for all 9 entities
- ✅ Database connection verification
- ✅ Pass/Fail reporting with detailed output
- ✅ Automatic cleanup of test data

### Test Results (Last Run)

```
========================================
   MARKETPLACE FULL CRUD TESTS
========================================
Total Tests:  36
Passed:       36 [OK]
Failed:       0 [FAIL]
Success Rate: 100%
========================================
ALL TESTS PASSED!
```

---

## 👥 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is developed for educational purposes at **FIRMA** (Tunisian Agricultural Marketplace).

---

## 👨‍💻 Authors

- Development Team - FIRMA Project 

---

**🌾 FIRMA - Your Agricultural Marketplace Solution 🌾**
