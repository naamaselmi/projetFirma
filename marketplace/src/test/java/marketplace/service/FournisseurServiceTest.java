package marketplace.service;

import marketplace.entities.Fournisseur;
import marketplace.tools.DB_connection;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour FournisseurService
 * Ordre logique : Create → Read → Update → Delete
 */
@TestMethodOrder(OrderAnnotation.class)
public class FournisseurServiceTest {

    private static FournisseurService fournisseurService;
    private static int testFournisseurId = -1;

    private static final String TEST_PREFIX = "JUNIT_TEST_";

    @BeforeAll
    static void setUp() {
        fournisseurService = new FournisseurService();
    }

    /**
     * Nettoyage automatique après chaque test :
     * supprime les fournisseurs de test (préfixe JUNIT_TEST_)
     */
    @AfterEach
    void cleanUp() throws SQLException {
        String sql = "DELETE FROM fournisseurs WHERE nom_entreprise LIKE ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(sql);
        pst.setString(1, TEST_PREFIX + "%");
        pst.executeUpdate();
    }

    // ========== 1. CREATE ==========

    @Test
    @Order(1)
    void testAddFournisseur() throws SQLException {
        // Arrange
        Fournisseur fournisseur = new Fournisseur(
                TEST_PREFIX + "AgriTech SA", "Ali Ben Salah",
                "ali@agritech-test.com", "+216 71 000 000");
        fournisseur.setAdresse("Zone Industrielle Test");
        fournisseur.setVille("Tunis");
        fournisseur.setActif(true);

        // Act
        fournisseurService.addEntity(fournisseur);

        // Assert
        List<Fournisseur> fournisseurs = fournisseurService.getEntities();
        boolean found = fournisseurs.stream()
                .anyMatch(f -> f.getNomEntreprise().equals(TEST_PREFIX + "AgriTech SA"));
        assertTrue(found, "Le fournisseur de test doit être présent après l'ajout");
    }

    // ========== 2. READ ==========

    @Test
    @Order(2)
    void testGetFournisseurs() throws SQLException {
        // Arrange — insérer un fournisseur pour garantir un résultat
        Fournisseur fournisseur = new Fournisseur(
                TEST_PREFIX + "FertiPlus", "Sami Trabelsi",
                "sami@fertiplus-test.com", "+216 72 000 000");
        fournisseur.setAdresse("Rue Test 42");
        fournisseur.setVille("Sfax");
        fournisseur.setActif(true);
        fournisseurService.addEntity(fournisseur);

        // Act
        List<Fournisseur> fournisseurs = fournisseurService.getEntities();

        // Assert
        assertNotNull(fournisseurs, "La liste des fournisseurs ne doit pas être null");
        assertFalse(fournisseurs.isEmpty(), "La liste des fournisseurs ne doit pas être vide");
    }

    // ========== 3. UPDATE ==========

    @Test
    @Order(3)
    void testUpdateFournisseur() throws SQLException {
        // Arrange — créer un fournisseur puis récupérer son ID
        Fournisseur fournisseur = new Fournisseur(
                TEST_PREFIX + "MecaAgri", "Nadia Khelifi",
                "nadia@mecaagri-test.com", "+216 73 000 000");
        fournisseur.setAdresse("Avenue Test");
        fournisseur.setVille("Sousse");
        fournisseur.setActif(true);
        fournisseurService.addEntity(fournisseur);

        List<Fournisseur> fournisseurs = fournisseurService.getEntities();
        Fournisseur inserted = fournisseurs.stream()
                .filter(f -> f.getNomEntreprise().equals(TEST_PREFIX + "MecaAgri"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fournisseur de test non trouvé pour update"));

        testFournisseurId = inserted.getId();

        // Act — modifier le nom et la ville
        inserted.setNomEntreprise(TEST_PREFIX + "MecaAgri_Modifie");
        inserted.setVille("Monastir");
        fournisseurService.updateEntity(inserted);

        // Assert
        List<Fournisseur> updatedList = fournisseurService.getEntities();
        Fournisseur updated = updatedList.stream()
                .filter(f -> f.getId() == testFournisseurId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fournisseur modifié non trouvé"));

        assertEquals(TEST_PREFIX + "MecaAgri_Modifie", updated.getNomEntreprise(),
                "Le nom de l'entreprise doit être mis à jour");
        assertEquals("Monastir", updated.getVille(),
                "La ville doit être mise à jour");
    }

    // ========== 4. DELETE ==========

    @Test
    @Order(4)
    void testDeleteFournisseur() throws SQLException {
        // Arrange
        Fournisseur fournisseur = new Fournisseur(
                TEST_PREFIX + "ASupprimer", "Test Contact",
                "delete@test.com", "+216 70 000 000");
        fournisseur.setAdresse("Adresse de test");
        fournisseur.setVille("Bizerte");
        fournisseur.setActif(true);
        fournisseurService.addEntity(fournisseur);

        List<Fournisseur> fournisseurs = fournisseurService.getEntities();
        Fournisseur toDelete = fournisseurs.stream()
                .filter(f -> f.getNomEntreprise().equals(TEST_PREFIX + "ASupprimer"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fournisseur à supprimer non trouvé"));

        // Act
        fournisseurService.deleteEntity(toDelete);

        // Assert
        List<Fournisseur> afterDelete = fournisseurService.getEntities();
        boolean stillExists = afterDelete.stream()
                .anyMatch(f -> f.getId() == toDelete.getId());
        assertFalse(stillExists, "Le fournisseur doit avoir été supprimé");
    }
}
