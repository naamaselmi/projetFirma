# 🏪 Marketplace Management System

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.14-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-green)
![Maven](https://img.shields.io/badge/Maven-3.x-red)

A comprehensive JavaFX desktop application for managing agricultural marketplace operations including equipment sales, vehicle rentals, and terrain leasing. Built with modern Java architecture following MVC pattern.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Technologies](#-technologies)
- [Getting Started](#-getting-started)
- [Database Schema](#-database-schema)
- [API Reference](#-api-reference)
- [Testing](#-testing)
- [Contributing](#-contributing)

---

## ✨ Features

### Core Business Features
- **🛒 Equipment Management** - Full CRUD operations for agricultural equipment inventory
- **🚗 Vehicle Rentals** - Manage vehicle fleet with daily/weekly/monthly pricing
- **🌾 Terrain Leasing** - Handle agricultural land rental operations
- **📦 Order Management** - Complete order lifecycle with payment & delivery tracking
- **👥 Supplier Management** - Maintain supplier relationships and purchase records
- **📊 Category System** - Organize products by type (Equipment, Vehicles, Terrain)

### User Management
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
│   ├── Launcher.java                 # Application entry point
│   ├── GUI/
│   │   ├── Application/
│   │   │   └── LoginApplication.java # JavaFX Application class
│   │   └── Controller/
│   │       ├── LoginController.java
│   │       ├── AdminDashboardController.java
│   │       └── ClientDashboardController.java
│   ├── entities/                     # Domain models (13 classes)
│   │   ├── Categorie.java
│   │   ├── Fournisseur.java
│   │   ├── Equipement.java
│   │   ├── Vehicule.java
│   │   ├── Terrain.java
│   │   ├── Commande.java
│   │   ├── DetailCommande.java
│   │   ├── Location.java
│   │   ├── AchatFournisseur.java
│   │   ├── ProductType.java          # Enum
│   │   ├── PaymentStatus.java        # Enum
│   │   ├── DeliveryStatus.java       # Enum
│   │   └── RentalStatus.java         # Enum
│   ├── service/                      # Business logic (9 services)
│   │   ├── CategorieService.java
│   │   ├── FournisseurService.java
│   │   ├── EquipementService.java
│   │   ├── VehiculeService.java
│   │   ├── TerrainService.java
│   │   ├── CommandeService.java
│   │   ├── DetailCommandeService.java
│   │   ├── LocationService.java
│   │   └── AchatFournisseurService.java
│   ├── interfaces/
│   │   └── IService.java             # Generic service interface
│   ├── tools/
│   │   └── DB_connection.java        # Database singleton
│   └── test/
│       └── TestMain.java             # CRUD test suite
├── src/main/resources/
│   ├── marketplace/GUI/views/        # FXML files
│   │   ├── login.fxml
│   │   ├── admin_dashboard.fxml
│   │   └── client_dashboard.fxml
│   └── image/                        # Application assets
│       ├── logo.png
│       ├── slogan.png
│       └── firma.png
└── pom.xml                           # Maven configuration
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
| VehiculeService | `getAvailableVehicules()`, `search()` |
| TerrainService | `getAvailableTerrains()`, `searchByVille()` |
| CommandeService | `getCommandesByUser()`, `getPendingCommandes()`, `updatePaymentStatus()` |
| LocationService | `getLocationsByUser()`, `updateStatus()` |

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
