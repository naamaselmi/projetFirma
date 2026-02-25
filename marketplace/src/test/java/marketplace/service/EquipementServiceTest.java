package marketplace.service;

import marketplace.entities.Categorie;
import marketplace.entities.Equipement;
import marketplace.entities.Fournisseur;
import marketplace.entities.ProductType;
import marketplace.tools.DB_connection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour EquipementService
 * Ordre logique : Create → Read → Update → Delete
 *
 * Note : Equipement a des FK vers categories et fournisseurs.
 * 
 * @BeforeAll insère les données parentes nécessaires,
 * @AfterAll les nettoie.
 */
@TestMethodOrder(OrderAnnotation.class)
public class EquipementServiceTest {

    private static EquipementService equipementService;
    private static CategorieService categorieService;
    private static FournisseurService fournisseurService;

    private static int testCategorieId;
    private static int testFournisseurId;
    private static int testEquipementId = -1;

    private static final String TEST_PREFIX = "JUNIT_TEST_";

    @BeforeAll
    static void setUp() throws SQLException {
        equipementService = new EquipementService();
        categorieService = new CategorieService();
        fournisseurService = new FournisseurService();

        // --- Créer une catégorie parente de test ---
        Categorie categorie = new Categorie(
                TEST_PREFIX + "CatEquip", ProductType.EQUIPEMENT, "Catégorie parent pour test équipement");
        categorieService.addEntity(categorie);

        List<Categorie> categories = categorieService.getEntities();
        testCategorieId = categories.stream()
                .filter(c -> c.getNom().equals(TEST_PREFIX + "CatEquip"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Impossible de créer la catégorie de test"))
                .getId();

        // --- Créer un fournisseur parent de test ---
        Fournisseur fournisseur = new Fournisseur(
                TEST_PREFIX + "FournEquip", "Contact Test",
                "equip-test@junit.com", "+216 71 999 999");
        fournisseur.setAdresse("Adresse Test");
        fournisseur.setVille("Tunis");
        fournisseur.setActif(true);
        fournisseurService.addEntity(fournisseur);

        List<Fournisseur> fournisseurs = fournisseurService.getEntities();
        testFournisseurId = fournisseurs.stream()
                .filter(f -> f.getNomEntreprise().equals(TEST_PREFIX + "FournEquip"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Impossible de créer le fournisseur de test"))
                .getId();
    }

    /**
     * Nettoyage automatique après chaque test :
     * supprime les équipements de test (préfixe JUNIT_TEST_)
     */
    @AfterEach
    void cleanUp() throws SQLException {
        String sql = "DELETE FROM equipements WHERE nom LIKE ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(sql);
        pst.setString(1, TEST_PREFIX + "%");
        pst.executeUpdate();
    }

    /**
     * Nettoyage final : supprimer les données parentes (catégorie + fournisseur de
     * test)
     */
    @AfterAll
    static void tearDown() throws SQLException {
        // Supprimer les équipements restants au cas où
        String sqlEquip = "DELETE FROM equipements WHERE nom LIKE ?";
        PreparedStatement pstEquip = DB_connection.getInstance().getConnection().prepareStatement(sqlEquip);
        pstEquip.setString(1, TEST_PREFIX + "%");
        pstEquip.executeUpdate();

        // Supprimer la catégorie de test
        String sqlCat = "DELETE FROM categories WHERE nom LIKE ?";
        PreparedStatement pstCat = DB_connection.getInstance().getConnection().prepareStatement(sqlCat);
        pstCat.setString(1, TEST_PREFIX + "%");
        pstCat.executeUpdate();

        // Supprimer le fournisseur de test
        String sqlFourn = "DELETE FROM fournisseurs WHERE nom_entreprise LIKE ?";
        PreparedStatement pstFourn = DB_connection.getInstance().getConnection().prepareStatement(sqlFourn);
        pstFourn.setString(1, TEST_PREFIX + "%");
        pstFourn.executeUpdate();
    }

    // ========== 1. CREATE ==========

    @Test
    @Order(1)
    void testAddEquipement() throws SQLException {
        // Arrange
        Equipement equipement = new Equipement(
                testCategorieId, testFournisseurId,
                TEST_PREFIX + "Tracteur A1",
                "Tracteur de test pour JUnit",
                new BigDecimal("15000.00"),
                new BigDecimal("18500.00"),
                10);
        equipement.setSeuilAlerte(3);
        equipement.setImageUrl(null);
        equipement.setDisponible(true);

        // Act
        equipementService.addEntity(equipement);

        // Assert
        List<Equipement> equipements = equipementService.getEntities();
        boolean found = equipements.stream()
                .anyMatch(e -> e.getNom().equals(TEST_PREFIX + "Tracteur A1"));
        assertTrue(found, "L'équipement de test doit être présent après l'ajout");
    }

    // ========== 2. READ ==========

    @Test
    @Order(2)
    void testGetEquipements() throws SQLException {
        // Arrange — insérer un équipement pour garantir un résultat
        Equipement equipement = new Equipement(
                testCategorieId, testFournisseurId,
                TEST_PREFIX + "Moissonneuse B2",
                "Moissonneuse de test",
                new BigDecimal("25000.00"),
                new BigDecimal("30000.00"),
                5);
        equipement.setSeuilAlerte(2);
        equipement.setDisponible(true);
        equipementService.addEntity(equipement);

        // Act
        List<Equipement> equipements = equipementService.getEntities();

        // Assert
        assertNotNull(equipements, "La liste des équipements ne doit pas être null");
        assertFalse(equipements.isEmpty(), "La liste des équipements ne doit pas être vide");
    }

    // ========== 3. UPDATE ==========

    @Test
    @Order(3)
    void testUpdateEquipement() throws SQLException {
        // Arrange — créer un équipement puis récupérer son ID
        Equipement equipement = new Equipement(
                testCategorieId, testFournisseurId,
                TEST_PREFIX + "Semoir C3",
                "Semoir de test avant modification",
                new BigDecimal("8000.00"),
                new BigDecimal("10500.00"),
                15);
        equipement.setSeuilAlerte(5);
        equipement.setDisponible(true);
        equipementService.addEntity(equipement);

        List<Equipement> equipements = equipementService.getEntities();
        Equipement inserted = equipements.stream()
                .filter(e -> e.getNom().equals(TEST_PREFIX + "Semoir C3"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Équipement de test non trouvé pour update"));

        testEquipementId = inserted.getId();

        // Act — modifier le nom et le prix de vente
        inserted.setNom(TEST_PREFIX + "Semoir C3_Modifie");
        inserted.setPrixVente(new BigDecimal("11000.00"));
        inserted.setDescription("Semoir de test après modification");
        equipementService.updateEntity(inserted);

        // Assert
        List<Equipement> updatedList = equipementService.getEntities();
        Equipement updated = updatedList.stream()
                .filter(e -> e.getId() == testEquipementId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Équipement modifié non trouvé"));

        assertEquals(TEST_PREFIX + "Semoir C3_Modifie", updated.getNom(),
                "Le nom de l'équipement doit être mis à jour");
        assertEquals(new BigDecimal("11000.00"), updated.getPrixVente(),
                "Le prix de vente doit être mis à jour");
        assertEquals("Semoir de test après modification", updated.getDescription(),
                "La description doit être mise à jour");
    }

    // ========== 4. DELETE ==========

    @Test
    @Order(4)
    void testDeleteEquipement() throws SQLException {
        // Arrange
        Equipement equipement = new Equipement(
                testCategorieId, testFournisseurId,
                TEST_PREFIX + "ASupprimer",
                "Équipement à supprimer",
                new BigDecimal("5000.00"),
                new BigDecimal("6500.00"),
                3);
        equipement.setSeuilAlerte(1);
        equipement.setDisponible(true);
        equipementService.addEntity(equipement);

        List<Equipement> equipements = equipementService.getEntities();
        Equipement toDelete = equipements.stream()
                .filter(e -> e.getNom().equals(TEST_PREFIX + "ASupprimer"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Équipement à supprimer non trouvé"));

        // Act
        equipementService.deleteEntity(toDelete);

        // Assert
        List<Equipement> afterDelete = equipementService.getEntities();
        boolean stillExists = afterDelete.stream()
                .anyMatch(e -> e.getId() == toDelete.getId());
        assertFalse(stillExists, "L'équipement doit avoir été supprimé");
    }
}
