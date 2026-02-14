package marketplace.test;

import marketplace.entities.*;
import marketplace.service.*;
import marketplace.tools.DB_connection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Test Main class to verify all service functionalities
 * Tests FULL CRUD operations (Add, Update, Delete, List) for all entities
 * 
 * Note: This test uses the database connection. Make sure MySQL is running.
 * Test user: admin@firma.tn / admin123
 */
public class TestMain {

    // Counters for test results
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   MARKETPLACE FULL CRUD TESTS");
        System.out.println("========================================\n");

        // Test database connection first
        testDatabaseConnection();

        // Test each service with FULL CRUD
        testCategorieServiceCRUD();
        testFournisseurServiceCRUD();
        testEquipementServiceCRUD();
        testVehiculeServiceCRUD();
        testTerrainServiceCRUD();
        testCommandeServiceCRUD();
        testDetailCommandeServiceCRUD();
        testLocationServiceCRUD();
        testAchatFournisseurServiceCRUD();

        // Close database connection
        DB_connection.getInstance().closeConnection();

        // Print final summary
        printFinalSummary();
    }

    private static void testDatabaseConnection() {
        System.out.println("--- Testing Database Connection ---");
        try {
            if (DB_connection.getInstance().getConnection() != null) {
                System.out.println("[OK] Database connection successful!\n");
            } else {
                System.out.println("[FAILED] Database connection is null!\n");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage() + "\n");
        }
    }

    private static void printFinalSummary() {
        System.out.println("\n========================================");
        System.out.println("           FINAL TEST SUMMARY");
        System.out.println("========================================");
        System.out.println("Total Tests:  " + totalTests);
        System.out.println("Passed:       " + passedTests + " [OK]");
        System.out.println("Failed:       " + failedTests + " [FAIL]");
        System.out.println("Success Rate: " + (totalTests > 0 ? (passedTests * 100 / totalTests) : 0) + "%");
        System.out.println("========================================");

        if (failedTests == 0) {
            System.out.println("ALL TESTS PASSED! CRUD operations work for all entities.");
        } else {
            System.out.println("Some tests failed. Please review the errors above.");
        }
    }

    private static void reportTest(String testName, boolean success, String details) {
        totalTests++;
        if (success) {
            passedTests++;
            System.out.println("   [PASS] " + testName + (details != null ? " - " + details : ""));
        } else {
            failedTests++;
            System.out.println("   [FAIL] " + testName + (details != null ? " - " + details : ""));
        }
    }

    // Helper method to find entity by ID in list
    private static Categorie findCategorieById(List<Categorie> list, int id) {
        for (Categorie c : list) {
            if (c.getId() == id)
                return c;
        }
        return null;
    }

    private static Categorie findCategorieByNom(List<Categorie> list, String nom) {
        for (Categorie c : list) {
            if (nom.equals(c.getNom()))
                return c;
        }
        return null;
    }

    private static Fournisseur findFournisseurById(List<Fournisseur> list, int id) {
        for (Fournisseur f : list) {
            if (f.getId() == id)
                return f;
        }
        return null;
    }

    private static Fournisseur findFournisseurByNom(List<Fournisseur> list, String nom) {
        for (Fournisseur f : list) {
            if (nom.equals(f.getNomEntreprise()))
                return f;
        }
        return null;
    }

    private static Equipement findEquipementById(List<Equipement> list, int id) {
        for (Equipement e : list) {
            if (e.getId() == id)
                return e;
        }
        return null;
    }

    private static Equipement findEquipementByNom(List<Equipement> list, String nom) {
        for (Equipement e : list) {
            if (nom.equals(e.getNom()))
                return e;
        }
        return null;
    }

    private static Vehicule findVehiculeById(List<Vehicule> list, int id) {
        for (Vehicule v : list) {
            if (v.getId() == id)
                return v;
        }
        return null;
    }

    private static Vehicule findVehiculeByNom(List<Vehicule> list, String nom) {
        for (Vehicule v : list) {
            if (nom.equals(v.getNom()))
                return v;
        }
        return null;
    }

    private static Terrain findTerrainById(List<Terrain> list, int id) {
        for (Terrain t : list) {
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    private static Terrain findTerrainByTitre(List<Terrain> list, String titre) {
        for (Terrain t : list) {
            if (titre.equals(t.getTitre()))
                return t;
        }
        return null;
    }

    private static Commande findCommandeById(List<Commande> list, int id) {
        for (Commande c : list) {
            if (c.getId() == id)
                return c;
        }
        return null;
    }

    private static DetailCommande findDetailById(List<DetailCommande> list, int id) {
        for (DetailCommande d : list) {
            if (d.getId() == id)
                return d;
        }
        return null;
    }

    private static Location findLocationById(List<Location> list, int id) {
        for (Location l : list) {
            if (l.getId() == id)
                return l;
        }
        return null;
    }

    private static AchatFournisseur findAchatById(List<AchatFournisseur> list, int id) {
        for (AchatFournisseur a : list) {
            if (a.getId() == id)
                return a;
        }
        return null;
    }

    private static AchatFournisseur findAchatByFacture(List<AchatFournisseur> list, String numero) {
        for (AchatFournisseur a : list) {
            if (numero.equals(a.getNumeroFacture()))
                return a;
        }
        return null;
    }

    // ==================== CATEGORIE SERVICE CRUD ====================
    private static void testCategorieServiceCRUD() {
        System.out.println("\n--- Testing CategorieService CRUD ---");
        CategorieService service = new CategorieService();
        int testId = -1;

        try {
            // CREATE
            Categorie testCategorie = new Categorie("TEST_CATEGORIE", ProductType.EQUIPEMENT,
                    "Test description for CRUD");
            service.addEntity(testCategorie);

            // Verify creation by finding it in the list
            List<Categorie> categories = service.getEntities();
            Categorie found = findCategorieByNom(categories, "TEST_CATEGORIE");

            reportTest("ADD Categorie", found != null,
                    found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setDescription("Updated test description");
                service.updateEntity(found);

                categories = service.getEntities();
                Categorie updated = findCategorieById(categories, testId);

                boolean updateSuccess = updated != null && "Updated test description".equals(updated.getDescription());
                reportTest("UPDATE Categorie", updateSuccess,
                        updateSuccess ? "Description updated" : "Update verification failed");

                // LIST
                reportTest("LIST Categories", categories.size() > 0, "Found " + categories.size() + " categories");

                // DELETE
                service.deleteEntity(found);
                categories = service.getEntities();
                boolean deleted = findCategorieById(categories, testId) == null;
                reportTest("DELETE Categorie", deleted, deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("CategorieService CRUD", false, "Exception: " + e.getMessage());
            // Cleanup attempt
            if (testId > 0) {
                try {
                    Categorie cleanup = new Categorie();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== FOURNISSEUR SERVICE CRUD ====================
    private static void testFournisseurServiceCRUD() {
        System.out.println("\n--- Testing FournisseurService CRUD ---");
        FournisseurService service = new FournisseurService();
        int testId = -1;

        try {
            // CREATE
            Fournisseur testFournisseur = new Fournisseur("TEST_ENTREPRISE", "Test Contact", "test@test.com",
                    "+216 99 999 999");
            testFournisseur.setAdresse("123 Test Street");
            testFournisseur.setVille("Test City");
            service.addEntity(testFournisseur);

            // Verify creation
            List<Fournisseur> fournisseurs = service.getEntities();
            Fournisseur found = findFournisseurByNom(fournisseurs, "TEST_ENTREPRISE");

            reportTest("ADD Fournisseur", found != null,
                    found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setContactNom("Updated Contact Name");
                found.setActif(false);
                service.updateEntity(found);

                fournisseurs = service.getEntities();
                Fournisseur updated = findFournisseurById(fournisseurs, testId);

                boolean updateSuccess = updated != null && "Updated Contact Name".equals(updated.getContactNom())
                        && !updated.isActif();
                reportTest("UPDATE Fournisseur", updateSuccess,
                        updateSuccess ? "Contact and status updated" : "Update verification failed");

                // LIST
                reportTest("LIST Fournisseurs", fournisseurs.size() > 0,
                        "Found " + fournisseurs.size() + " fournisseurs");

                // DELETE
                service.deleteEntity(found);
                fournisseurs = service.getEntities();
                boolean deleted = findFournisseurById(fournisseurs, testId) == null;
                reportTest("DELETE Fournisseur", deleted,
                        deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("FournisseurService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Fournisseur cleanup = new Fournisseur();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== EQUIPEMENT SERVICE CRUD ====================
    private static void testEquipementServiceCRUD() {
        System.out.println("\n--- Testing EquipementService CRUD ---");
        EquipementService service = new EquipementService();
        CategorieService categorieService = new CategorieService();
        FournisseurService fournisseurService = new FournisseurService();
        int testId = -1;

        try {
            // Get existing category and supplier IDs for FK constraints
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.EQUIPEMENT);
            List<Fournisseur> fournisseurs = fournisseurService.getActiveFournisseurs();

            if (categories.isEmpty() || fournisseurs.isEmpty()) {
                reportTest("EQUIPEMENT Prerequisites", false, "Need at least one category and one supplier");
                return;
            }

            int categorieId = categories.get(0).getId();
            int fournisseurId = fournisseurs.get(0).getId();

            // CREATE
            Equipement testEquipement = new Equipement(categorieId, fournisseurId, "TEST_EQUIPEMENT",
                    "Test equipment description", new BigDecimal("1000.00"), new BigDecimal("1500.00"), 10);
            service.addEntity(testEquipement);

            // Verify creation
            List<Equipement> equipements = service.getEntities();
            Equipement found = findEquipementByNom(equipements, "TEST_EQUIPEMENT");

            reportTest("ADD Equipement", found != null,
                    found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setPrixVente(new BigDecimal("1750.00"));
                found.setQuantiteStock(15);
                service.updateEntity(found);

                equipements = service.getEntities();
                Equipement updated = findEquipementById(equipements, testId);

                boolean updateSuccess = updated != null &&
                        updated.getPrixVente().compareTo(new BigDecimal("1750.00")) == 0 &&
                        updated.getQuantiteStock() == 15;
                reportTest("UPDATE Equipement", updateSuccess,
                        updateSuccess ? "Price and stock updated" : "Update verification failed");

                // LIST
                reportTest("LIST Equipements", equipements.size() > 0, "Found " + equipements.size() + " equipements");

                // DELETE
                service.deleteEntity(found);
                equipements = service.getEntities();
                boolean deleted = findEquipementById(equipements, testId) == null;
                reportTest("DELETE Equipement", deleted,
                        deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("EquipementService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Equipement cleanup = new Equipement();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== VEHICULE SERVICE CRUD ====================
    private static void testVehiculeServiceCRUD() {
        System.out.println("\n--- Testing VehiculeService CRUD ---");
        VehiculeService service = new VehiculeService();
        CategorieService categorieService = new CategorieService();
        int testId = -1;

        try {
            // Get existing category for FK constraint
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.VEHICULE);

            if (categories.isEmpty()) {
                reportTest("VEHICULE Prerequisites", false, "Need at least one vehicule category");
                return;
            }

            int categorieId = categories.get(0).getId();

            // CREATE
            Vehicule testVehicule = new Vehicule(categorieId, "TEST_VEHICULE", "TestMarque", "TestModele",
                    "TEST-123-TN", new BigDecimal("250.00"));
            testVehicule.setDescription("Test vehicle description");
            testVehicule.setPrixSemaine(new BigDecimal("1500.00"));
            testVehicule.setPrixMois(new BigDecimal("5000.00"));
            service.addEntity(testVehicule);

            // Verify creation
            List<Vehicule> vehicules = service.getEntities();
            Vehicule found = findVehiculeByNom(vehicules, "TEST_VEHICULE");

            reportTest("ADD Vehicule", found != null, found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setPrixJour(new BigDecimal("300.00"));
                found.setDisponible(false);
                service.updateEntity(found);

                vehicules = service.getEntities();
                Vehicule updated = findVehiculeById(vehicules, testId);

                boolean updateSuccess = updated != null &&
                        updated.getPrixJour().compareTo(new BigDecimal("300.00")) == 0 &&
                        !updated.isDisponible();
                reportTest("UPDATE Vehicule", updateSuccess,
                        updateSuccess ? "Price and availability updated" : "Update verification failed");

                // LIST
                reportTest("LIST Vehicules", vehicules.size() > 0, "Found " + vehicules.size() + " vehicules");

                // DELETE
                service.deleteEntity(found);
                vehicules = service.getEntities();
                boolean deleted = findVehiculeById(vehicules, testId) == null;
                reportTest("DELETE Vehicule", deleted, deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("VehiculeService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Vehicule cleanup = new Vehicule();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== TERRAIN SERVICE CRUD ====================
    private static void testTerrainServiceCRUD() {
        System.out.println("\n--- Testing TerrainService CRUD ---");
        TerrainService service = new TerrainService();
        CategorieService categorieService = new CategorieService();
        int testId = -1;

        try {
            // Get existing category for FK constraint
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.TERRAIN);

            if (categories.isEmpty()) {
                reportTest("TERRAIN Prerequisites", false, "Need at least one terrain category");
                return;
            }

            int categorieId = categories.get(0).getId();

            // CREATE
            Terrain testTerrain = new Terrain(categorieId, "TEST_TERRAIN", new BigDecimal("50.5"),
                    "Test City", new BigDecimal("12000.00"));
            testTerrain.setDescription("Test terrain description");
            testTerrain.setAdresse("123 Test Address");
            testTerrain.setPrixMois(new BigDecimal("1000.00"));
            service.addEntity(testTerrain);

            // Verify creation
            List<Terrain> terrains = service.getEntities();
            Terrain found = findTerrainByTitre(terrains, "TEST_TERRAIN");

            reportTest("ADD Terrain", found != null, found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setPrixAnnee(new BigDecimal("15000.00"));
                found.setSuperficieHectares(new BigDecimal("75.0"));
                service.updateEntity(found);

                terrains = service.getEntities();
                Terrain updated = findTerrainById(terrains, testId);

                boolean updateSuccess = updated != null &&
                        updated.getPrixAnnee().compareTo(new BigDecimal("15000.00")) == 0;
                reportTest("UPDATE Terrain", updateSuccess,
                        updateSuccess ? "Price and area updated" : "Update verification failed");

                // LIST
                reportTest("LIST Terrains", terrains.size() > 0, "Found " + terrains.size() + " terrains");

                // DELETE
                service.deleteEntity(found);
                terrains = service.getEntities();
                boolean deleted = findTerrainById(terrains, testId) == null;
                reportTest("DELETE Terrain", deleted, deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("TerrainService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Terrain cleanup = new Terrain();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== COMMANDE SERVICE CRUD ====================
    private static void testCommandeServiceCRUD() {
        System.out.println("\n--- Testing CommandeService CRUD ---");
        CommandeService service = new CommandeService();
        int testId = -1;

        try {
            // CREATE (using user ID 1 - assuming admin exists)
            Commande testCommande = new Commande(1, "123 Test Delivery Address", "Test Delivery City");
            testCommande.setMontantTotal(new BigDecimal("500.00"));
            testCommande.setNotes("Test order notes");
            service.addEntity(testCommande);

            // Verify creation - find the most recent one with our test data
            List<Commande> commandes = service.getEntities();
            Commande found = null;
            for (Commande c : commandes) {
                if ("Test Delivery City".equals(c.getVilleLivraison()) &&
                        c.getMontantTotal() != null &&
                        c.getMontantTotal().compareTo(new BigDecimal("500.00")) == 0) {
                    found = c;
                    break;
                }
            }

            reportTest("ADD Commande", found != null,
                    found != null ? "ID=" + found.getId() + ", Numero=" + found.getNumeroCommande()
                            : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setStatutPaiement(PaymentStatus.PAYE);
                found.setStatutLivraison(DeliveryStatus.EN_PREPARATION);
                found.setMontantTotal(new BigDecimal("750.00"));
                service.updateEntity(found);

                commandes = service.getEntities();
                Commande updated = findCommandeById(commandes, testId);

                boolean updateSuccess = updated != null &&
                        updated.getStatutPaiement() == PaymentStatus.PAYE &&
                        updated.getStatutLivraison() == DeliveryStatus.EN_PREPARATION;
                reportTest("UPDATE Commande", updateSuccess,
                        updateSuccess ? "Status updated" : "Update verification failed");

                // LIST
                reportTest("LIST Commandes", commandes.size() > 0, "Found " + commandes.size() + " commandes");

                // DELETE
                service.deleteEntity(found);
                commandes = service.getEntities();
                boolean deleted = findCommandeById(commandes, testId) == null;
                reportTest("DELETE Commande", deleted, deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("CommandeService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Commande cleanup = new Commande();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== DETAIL COMMANDE SERVICE CRUD ====================
    private static void testDetailCommandeServiceCRUD() {
        System.out.println("\n--- Testing DetailCommandeService CRUD ---");
        DetailCommandeService service = new DetailCommandeService();
        CommandeService commandeService = new CommandeService();
        EquipementService equipementService = new EquipementService();
        int testDetailId = -1;
        int tempCommandeId = -1;

        try {
            // Get existing equipement
            List<Equipement> equipements = equipementService.getEntities();

            if (equipements.isEmpty()) {
                reportTest("DETAIL_COMMANDE Prerequisites", false, "Need at least one equipement");
                return;
            }

            // Create a temporary order for testing
            Commande tempCommande = new Commande(1, "Temp Test Address", "Temp Test City");
            commandeService.addEntity(tempCommande);

            List<Commande> commandes = commandeService.getEntities();
            for (Commande c : commandes) {
                if ("Temp Test Address".equals(c.getAdresseLivraison())) {
                    tempCommande = c;
                    tempCommandeId = c.getId();
                    break;
                }
            }

            if (tempCommandeId < 0) {
                reportTest("DETAIL_COMMANDE Prerequisites", false, "Could not create test commande");
                return;
            }

            int equipementId = equipements.get(0).getId();

            // CREATE
            DetailCommande testDetail = new DetailCommande(tempCommandeId, equipementId, 3, new BigDecimal("150.00"));
            service.addEntity(testDetail);

            // Verify creation
            List<DetailCommande> details = service.getEntities();
            DetailCommande found = null;
            for (DetailCommande d : details) {
                if (d.getCommandeId() == tempCommandeId && d.getEquipementId() == equipementId) {
                    found = d;
                    break;
                }
            }

            reportTest("ADD DetailCommande", found != null,
                    found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testDetailId = found.getId();

                // UPDATE
                found.setQuantite(5);
                found.setPrixUnitaire(new BigDecimal("175.00"));
                service.updateEntity(found);

                details = service.getEntities();
                DetailCommande updated = findDetailById(details, testDetailId);

                boolean updateSuccess = updated != null && updated.getQuantite() == 5;
                reportTest("UPDATE DetailCommande", updateSuccess,
                        updateSuccess ? "Quantity updated" : "Update verification failed");

                // LIST
                reportTest("LIST DetailCommandes", details.size() > 0, "Found " + details.size() + " details");

                // DELETE
                service.deleteEntity(found);
                details = service.getEntities();
                boolean deleted = findDetailById(details, testDetailId) == null;
                reportTest("DELETE DetailCommande", deleted,
                        deleted ? "Successfully deleted" : "Still exists after delete");
            }

            // Cleanup temp commande
            if (tempCommandeId > 0) {
                Commande cleanup = new Commande();
                cleanup.setId(tempCommandeId);
                commandeService.deleteEntity(cleanup);
            }
        } catch (SQLException e) {
            reportTest("DetailCommandeService CRUD", false, "Exception: " + e.getMessage());
            // Cleanup
            if (testDetailId > 0) {
                try {
                    DetailCommande cleanup = new DetailCommande();
                    cleanup.setId(testDetailId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
            if (tempCommandeId > 0) {
                try {
                    Commande cleanup = new Commande();
                    cleanup.setId(tempCommandeId);
                    commandeService.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== LOCATION SERVICE CRUD ====================
    private static void testLocationServiceCRUD() {
        System.out.println("\n--- Testing LocationService CRUD ---");
        LocationService service = new LocationService();
        VehiculeService vehiculeService = new VehiculeService();
        int testId = -1;

        try {
            // Get existing vehicule for FK
            List<Vehicule> vehicules = vehiculeService.getAvailableVehicules();

            if (vehicules.isEmpty()) {
                reportTest("LOCATION Prerequisites", false, "Need at least one available vehicule");
                return;
            }

            int vehiculeId = vehicules.get(0).getId();

            // CREATE
            Location testLocation = new Location(1, "vehicule", vehiculeId,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(7), new BigDecimal("1500.00"));
            testLocation.setNotes("Test rental notes");
            service.addEntity(testLocation);

            // Verify creation
            List<Location> locations = service.getEntities();
            Location found = null;
            for (Location l : locations) {
                if (l.getElementId() == vehiculeId &&
                        "vehicule".equals(l.getTypeLocation()) &&
                        "Test rental notes".equals(l.getNotes())) {
                    found = l;
                    break;
                }
            }

            reportTest("ADD Location", found != null,
                    found != null ? "ID=" + found.getId() + ", Numero=" + found.getNumeroLocation()
                            : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setStatut(RentalStatus.CONFIRMEE);
                found.setPrixTotal(new BigDecimal("1750.00"));
                service.updateEntity(found);

                locations = service.getEntities();
                Location updated = findLocationById(locations, testId);

                boolean updateSuccess = updated != null && updated.getStatut() == RentalStatus.CONFIRMEE;
                reportTest("UPDATE Location", updateSuccess,
                        updateSuccess ? "Status updated" : "Update verification failed");

                // LIST
                reportTest("LIST Locations", locations.size() > 0, "Found " + locations.size() + " locations");

                // DELETE
                service.deleteEntity(found);
                locations = service.getEntities();
                boolean deleted = findLocationById(locations, testId) == null;
                reportTest("DELETE Location", deleted, deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("LocationService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    Location cleanup = new Location();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ==================== ACHAT FOURNISSEUR SERVICE CRUD ====================
    private static void testAchatFournisseurServiceCRUD() {
        System.out.println("\n--- Testing AchatFournisseurService CRUD ---");
        AchatFournisseurService service = new AchatFournisseurService();
        FournisseurService fournisseurService = new FournisseurService();
        EquipementService equipementService = new EquipementService();
        int testId = -1;

        try {
            // Get existing fournisseur and equipement for FK
            List<Fournisseur> fournisseurs = fournisseurService.getActiveFournisseurs();
            List<Equipement> equipements = equipementService.getEntities();

            if (fournisseurs.isEmpty() || equipements.isEmpty()) {
                reportTest("ACHAT_FOURNISSEUR Prerequisites", false, "Need at least one fournisseur and equipement");
                return;
            }

            int fournisseurId = fournisseurs.get(0).getId();
            int equipementId = equipements.get(0).getId();

            // CREATE
            AchatFournisseur testAchat = new AchatFournisseur(fournisseurId, equipementId, 20,
                    new BigDecimal("100.00"), LocalDate.now(), "TEST-FACTURE-001");
            service.addEntity(testAchat);

            // Verify creation
            List<AchatFournisseur> achats = service.getEntities();
            AchatFournisseur found = findAchatByFacture(achats, "TEST-FACTURE-001");

            reportTest("ADD AchatFournisseur", found != null,
                    found != null ? "ID=" + found.getId() : "Not found after insert");

            if (found != null) {
                testId = found.getId();

                // UPDATE
                found.setQuantite(25);
                found.setStatutPaiement(PaymentStatus.PAYE);
                service.updateEntity(found);

                achats = service.getEntities();
                AchatFournisseur updated = findAchatById(achats, testId);

                boolean updateSuccess = updated != null &&
                        updated.getQuantite() == 25 &&
                        updated.getStatutPaiement() == PaymentStatus.PAYE;
                reportTest("UPDATE AchatFournisseur", updateSuccess,
                        updateSuccess ? "Quantity and status updated" : "Update verification failed");

                // LIST
                reportTest("LIST AchatsFournisseurs", achats.size() > 0, "Found " + achats.size() + " achats");

                // DELETE
                service.deleteEntity(found);
                achats = service.getEntities();
                boolean deleted = findAchatById(achats, testId) == null;
                reportTest("DELETE AchatFournisseur", deleted,
                        deleted ? "Successfully deleted" : "Still exists after delete");
            }
        } catch (SQLException e) {
            reportTest("AchatFournisseurService CRUD", false, "Exception: " + e.getMessage());
            if (testId > 0) {
                try {
                    AchatFournisseur cleanup = new AchatFournisseur();
                    cleanup.setId(testId);
                    service.deleteEntity(cleanup);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
