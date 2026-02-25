package marketplace.service;

import marketplace.entities.Categorie;
import marketplace.entities.ProductType;
import marketplace.tools.DB_connection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CategorieService
 * Ordre logique : Create → Read → Update → Delete
 */
@TestMethodOrder(OrderAnnotation.class)
public class CategorieServiceTest {

    private static CategorieService categorieService;
    private static int testCategorieId = -1;

    // Identifiant unique pour éviter les collisions avec les données existantes
    private static final String TEST_PREFIX = "JUNIT_TEST_";

    @BeforeAll
    static void setUp() {
        categorieService = new CategorieService();
    }

    /**
     * Nettoyage automatique après chaque test :
     * supprime toutes les catégories de test restantes (préfixe JUNIT_TEST_)
     */
    @AfterEach
    void cleanUp() throws SQLException {
        String sql = "DELETE FROM categories WHERE nom LIKE ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(sql);
        pst.setString(1, TEST_PREFIX + "%");
        pst.executeUpdate();
    }

    // ========== 1. CREATE ==========

    @Test
    @Order(1)
    void testAddCategorie() throws SQLException {
        // Arrange
        Categorie categorie = new Categorie(
                TEST_PREFIX + "Outils", ProductType.EQUIPEMENT, "Catégorie de test JUnit");

        // Act
        categorieService.addEntity(categorie);

        // Assert — vérifier que la catégorie existe en base
        List<Categorie> categories = categorieService.getEntities();
        boolean found = categories.stream()
                .anyMatch(c -> c.getNom().equals(TEST_PREFIX + "Outils"));
        assertTrue(found, "La catégorie de test doit être présente après l'ajout");
    }

    // ========== 2. READ ==========

    @Test
    @Order(2)
    void testGetCategories() throws SQLException {
        // Arrange — insérer une catégorie pour s'assurer que la liste n'est pas vide
        Categorie categorie = new Categorie(
                TEST_PREFIX + "Semences", ProductType.EQUIPEMENT, "Catégorie lecture test");
        categorieService.addEntity(categorie);

        // Act
        List<Categorie> categories = categorieService.getEntities();

        // Assert
        assertNotNull(categories, "La liste des catégories ne doit pas être null");
        assertFalse(categories.isEmpty(), "La liste des catégories ne doit pas être vide");
    }

    // ========== 3. UPDATE ==========

    @Test
    @Order(3)
    void testUpdateCategorie() throws SQLException {
        // Arrange — créer puis récupérer l'ID de la catégorie de test
        Categorie categorie = new Categorie(
                TEST_PREFIX + "Engrais", ProductType.EQUIPEMENT, "Avant modification");
        categorieService.addEntity(categorie);

        // Récupérer l'ID auto-généré
        List<Categorie> categories = categorieService.getEntities();
        Categorie inserted = categories.stream()
                .filter(c -> c.getNom().equals(TEST_PREFIX + "Engrais"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Catégorie de test non trouvée pour update"));

        testCategorieId = inserted.getId();

        // Act — modifier le nom et la description
        inserted.setNom(TEST_PREFIX + "Engrais_Modifie");
        inserted.setDescription("Après modification");
        categorieService.updateEntity(inserted);

        // Assert — relire et vérifier
        List<Categorie> updatedList = categorieService.getEntities();
        Categorie updated = updatedList.stream()
                .filter(c -> c.getId() == testCategorieId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Catégorie modifiée non trouvée"));

        assertEquals(TEST_PREFIX + "Engrais_Modifie", updated.getNom(),
                "Le nom de la catégorie doit être mis à jour");
        assertEquals("Après modification", updated.getDescription(),
                "La description doit être mise à jour");
    }

    // ========== 4. DELETE ==========

    @Test
    @Order(4)
    void testDeleteCategorie() throws SQLException {
        // Arrange — créer une catégorie à supprimer
        Categorie categorie = new Categorie(
                TEST_PREFIX + "ASupprimer", ProductType.TERRAIN, "Catégorie à supprimer");
        categorieService.addEntity(categorie);

        List<Categorie> categories = categorieService.getEntities();
        Categorie toDelete = categories.stream()
                .filter(c -> c.getNom().equals(TEST_PREFIX + "ASupprimer"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Catégorie à supprimer non trouvée"));

        // Act
        categorieService.deleteEntity(toDelete);

        // Assert — vérifier l'absence
        List<Categorie> afterDelete = categorieService.getEntities();
        boolean stillExists = afterDelete.stream()
                .anyMatch(c -> c.getId() == toDelete.getId());
        assertFalse(stillExists, "La catégorie doit avoir été supprimée");
    }
}
