package Service;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.service.Demandeservice;
import edu.connection3a7.tools.MyConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemandeServiceTest {

    private Demandeservice demandeService;
    private Connection cnx;
    private Integer testDemandeId;

    // ✅ ID dynamiques (générés automatiquement)
    private int TEST_ID_UTILISATEUR;
    private int TEST_ID_TECH;
    private final String TEST_DESCRIPTION = "TEST_DEMANDE_" + System.currentTimeMillis();

    @BeforeAll
    void setUpBeforeClass() throws SQLException {
        cnx = MyConnection.getInstance().getCnx();
        demandeService = new Demandeservice();

        assertNotNull(cnx, "Connexion DB échouée");
        assertNotNull(demandeService, "Service non initialisé");

        // Créer et récupérer les ID automatiquement
        TEST_ID_UTILISATEUR = creerUtilisateurTest();
        TEST_ID_TECH = creerTechnicienTest();
    }

    private int creerUtilisateurTest() throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TestNom");
            ps.setString(2, "TestPrenom");
            ps.setString(3, "test@test.com");
            ps.setString(4, "password123");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Échec création utilisateur test");
    }

    private int creerTechnicienTest() throws SQLException {
        String sql = "INSERT INTO technicien (nom, prenom, email, specialite) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TestTech");
            ps.setString(2, "TestPrenomTech");
            ps.setString(3, "testtech@test.com");
            ps.setString(4, "Matériel");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Échec création technicien test");
    }

    @AfterAll
    void tearDownAfterClass() throws SQLException {
        // Nettoyer les demandes de test
        String sql1 = "DELETE FROM demande WHERE description LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql1)) {
            ps.setString(1, "TEST_DEMANDE_%");
            ps.executeUpdate();
        }

        // Nettoyer le technicien de test
        String sql2 = "DELETE FROM technicien WHERE id_tech = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql2)) {
            ps.setInt(1, TEST_ID_TECH);
            ps.executeUpdate();
        }

        // Nettoyer l'utilisateur de test
        String sql3 = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql3)) {
            ps.setInt(1, TEST_ID_UTILISATEUR);
            ps.executeUpdate();
        }
    }

    // ================= CREATE =================

    @Test
    @Order(1)
    void testCreate() throws SQLException {
        Demande demande = new Demande();
        demande.setIdUtilisateur(TEST_ID_UTILISATEUR);
        demande.setIdTech(TEST_ID_TECH);
        demande.setTypeProbleme("Matériel");
        demande.setDescription(TEST_DESCRIPTION);
        demande.setDateDemande(Date.valueOf(LocalDate.now()));
        demande.setStatut("En attente");

        int avant = demandeService.getdata().size();

        demandeService.addentitiy(demande);

        int apres = demandeService.getdata().size();

        assertEquals(avant + 1, apres);

        testDemandeId = demande.getIdDemande();
        assertNotNull(testDemandeId);
    }

    // ================= READ =================

    @Test
    @Order(2)
    void testReadAll() throws SQLException {
        List<Demande> demandes = demandeService.getdata();

        assertNotNull(demandes);
        assertTrue(demandes.size() > 0);
    }

    // ================= UPDATE =================

    @Test
    @Order(3)
    void testUpdate() throws SQLException {
        assertNotNull(testDemandeId);

        Demande demande = demandeService.getdata().stream()
                .filter(d -> d.getIdDemande().equals(testDemandeId))
                .findFirst()
                .orElse(null);

        assertNotNull(demande);

        demande.setStatut("Acceptée");
        demandeService.update(demande);

        Demande updated = demandeService.getdata().stream()
                .filter(d -> d.getIdDemande().equals(testDemandeId))
                .findFirst()
                .orElse(null);

        assertEquals("Acceptée", updated.getStatut());
    }

    // ================= DELETE =================

    @Test
    @Order(4)
    void testDelete() throws SQLException {
        assertNotNull(testDemandeId);

        Demande demande = new Demande();
        demande.setIdDemande(testDemandeId);

        int avant = demandeService.getdata().size();

        demandeService.delet(demande);

        int apres = demandeService.getdata().size();

        assertEquals(avant - 1, apres);

        testDemandeId = null;
    }
}